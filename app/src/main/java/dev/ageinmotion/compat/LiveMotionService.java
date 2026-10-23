package dev.ageinmotion.compat;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;

/**
 * User-initiated foreground service.
 * - no wake locks; redraws NEVER run with screen off or locked
 * - 2s for 90s after unlock, 10s up to five minutes, then pauses until next unlock
 * - 15s while Android Battery Saver is active
 * - never silently restarts after being killed
 *
 * Android does NOT expose a reliable unprivileged "launcher is visible" API.
 * This deliberately does not request Accessibility or usage-history privileges.
 */
public final class LiveMotionService extends Service {
    public static final String ACTION_STOP = "dev.ageinmotion.compat.STOP_LIVE";
    public static final String ACTION_UPDATE_NOTIFICATION = "dev.ageinmotion.compat.UPDATE_LIVE_NOTIFICATION";
    private static final String CHANNEL_ID = "age_motion_smart_v3";
    private static final int NOTIFICATION_ID = 1724;
    private static final long FAST_WINDOW = 90_000L;
    private static final long TOTAL_WINDOW = 300_000L;
    private static volatile boolean running;
    private static volatile boolean animating;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean registered;
    private long unlockedAt = -1;
    private PowerManager power;
    private KeyguardManager keyguard;

    private final Runnable redraw = new Runnable() {
        @Override public void run() {
            if (!running || !shouldDraw()) {
                animating = false;
                return;
            }
            long elapsed = SystemClock.elapsedRealtime() - unlockedAt;
            if (elapsed > TOTAL_WINDOW) {
                animating = false;
                AgeWidgetProvider.updateAll(LiveMotionService.this);
                return; // no runnable posted until next unlock
            }
            if (AgeCalculator.birthday(LiveMotionService.this) == null) {
                stopSelf(); return;
            }
            int[] ids = AppWidgetManager.getInstance(LiveMotionService.this).getAppWidgetIds(
                    new ComponentName(LiveMotionService.this, AgeWidgetProvider.class));
            if (ids.length == 0) {
                stopSelf(); return;
            }
            animating = true;
            AgeWidgetProvider.updateAll(LiveMotionService.this);
            long interval = power != null && power.isPowerSaveMode()
                    ? 15_000L : (elapsed <= FAST_WINDOW ? 2_000L : 10_000L);
            handler.postDelayed(this, interval);
        }
    };

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                unlockedAt = -1;
                animating = false;
                handler.removeCallbacks(redraw);
            } else if (Intent.ACTION_USER_PRESENT.equals(action)
                    || Intent.ACTION_SCREEN_ON.equals(action)) {
                if (shouldDraw()) beginWindow();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
                if (shouldDraw() && unlockedAt >= 0) scheduleRedraw();
            }
        }
    };

    static boolean isRunning() { return running; }
    static boolean isAnimating() { return animating; }

    @Override public void onCreate() {
        super.onCreate();
        power = (PowerManager)getSystemService(POWER_SERVICE);
        keyguard = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(screenReceiver, filter);
        }
        registered = true;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf(); return START_NOT_STICKY;
        }
        if (intent != null && ACTION_UPDATE_NOTIFICATION.equals(intent.getAction()) && running) {
            updateNotification(); return START_NOT_STICKY;
        }
        if (AgeCalculator.birthday(this) == null) {
            stopSelf(); return START_NOT_STICKY;
        }
        createChannel();
        try {
            startForeground(NOTIFICATION_ID, notification());
        } catch (RuntimeException denied) {
            running = false;
            stopSelf();
            return START_NOT_STICKY;
        }
        running = true;
        if (shouldDraw()) beginWindow();
        else AgeWidgetProvider.updateAll(this);
        return START_NOT_STICKY;
    }

    private boolean shouldDraw() {
        return power != null && power.isInteractive()
                && (keyguard == null || !keyguard.isKeyguardLocked());
    }

    private void beginWindow() {
        unlockedAt = SystemClock.elapsedRealtime();
        scheduleRedraw();
        updateNotification();
    }

    private void scheduleRedraw() {
        handler.removeCallbacks(redraw);
        if (running && shouldDraw() && unlockedAt >= 0) handler.post(redraw);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Age in Motion — Smart Motion", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Optional live age while the screen is unlocked");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification notification() {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(this, 741, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stop = new Intent(this, LiveMotionService.class).setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 742, stop,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        boolean revealAge = AgeCalculator.settings(this).getBoolean("lock_notification", false);
        AgeCalculator.AgeSnapshot age = AgeCalculator.snapshot(AgeCalculator.birthday(this));
        String title = revealAge ? "Age in Motion • " + age.fullYears + " years" : "Age in Motion • Smart Motion";
        String details = revealAge ? age.explanation() : "Home animation pauses when your phone locks";
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(details)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentIntent(openPi)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(revealAge ? Notification.VISIBILITY_PUBLIC : Notification.VISIBILITY_PRIVATE)
                .addAction(new Notification.Action.Builder(
                        android.R.drawable.ic_media_pause, "Stop", stopPi).build())
                .build();
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null && running) manager.notify(NOTIFICATION_ID, notification());
    }

    @Override public void onDestroy() {
        running = false;
        animating = false;
        handler.removeCallbacks(redraw);
        if (registered) {
            unregisterReceiver(screenReceiver);
            registered = false;
        }
        AgeWidgetProvider.updateAll(this);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
