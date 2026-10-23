package dev.ageinmotion.compat;

import android.Manifest;
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

/**
 * Optional, explicitly user-started foreground service for live home-screen age digits.
 * Redraws only when the screen is interactive; no boot receiver and no wake lock.
 *
 * Android's 30-minute AppWidgetProvider updatePeriodMillis cap does not apply to
 * updates made by a running app's own foreground service. The launcher or OS may
 * still throttle those updates, and the service uses more battery.
 */
public final class LiveMotionService extends Service {
    public static final String ACTION_STOP = "dev.ageinmotion.compat.STOP_LIVE";
    private static final String CHANNEL_ID = "age_motion_live_v2";
    private static final int NOTIFICATION_ID = 1724;
    private static final long INTERVAL_MS = 2000L;
    private static volatile boolean running;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean registered;
    private boolean screenOn;

    private final Runnable redraw = new Runnable() {
        @Override public void run() {
            if (!running || !screenOn) return;
            if (AgeCalculator.birthday(LiveMotionService.this) == null) {
                stopSelf();
                return;
            }
            AppWidgetManager manager = AppWidgetManager.getInstance(LiveMotionService.this);
            int[] ids = manager.getAppWidgetIds(
                    new ComponentName(LiveMotionService.this, AgeWidgetProvider.class));
            if (ids.length == 0) {
                stopSelf();
                return;
            }
            // Full updates are required for ImageView bitmaps to avoid RemoteViews
            // action cache accumulation from frequent partial updates.
            AgeWidgetProvider.updateAll(LiveMotionService.this);
            handler.postDelayed(this, INTERVAL_MS);
        }
    };

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                screenOn = false;
                handler.removeCallbacks(redraw);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                screenOn = true;
                scheduleRedraw();
            }
        }
    };

    public static boolean isRunning() { return running; }

    @Override public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(screenReceiver, filter);
        }
        registered = true;
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        screenOn = power != null && power.isInteractive();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (AgeCalculator.birthday(this) == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        createChannel();
        try {
            // Android 14+ uses the specialUse type declared in AndroidManifest.
            // startForeground() must be called promptly after startForegroundService().
            startForeground(NOTIFICATION_ID, notification());
        } catch (RuntimeException failed) {
            running = false;
            stopSelf();
            return START_NOT_STICKY;
        }
        running = true;
        if (screenOn) scheduleRedraw();
        return START_NOT_STICKY; // Never restart the service automatically after a system kill.
    }

    private void scheduleRedraw() {
        handler.removeCallbacks(redraw);
        if (running && screenOn) handler.post(redraw);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Age in Motion — Live Motion", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Ongoing notification while animated age digits are enabled");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification notification() {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(this, 741, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stop = new Intent(this, LiveMotionService.class);
        stop.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 742, stop,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Age in Motion • Live Motion ON")
                .setContentText("Age digits update every 2 seconds when screen is on")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentIntent(openPi)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(new Notification.Action.Builder(
                        android.R.drawable.ic_media_pause, "Stop Live Motion", stopPi).build())
                .build();
    }

    @Override public void onDestroy() {
        running = false;
        handler.removeCallbacks(redraw);
        if (registered) {
            unregisterReceiver(screenReceiver);
            registered = false;
        }
        // Return to a readable static 4-decimal widget after Live Motion is stopped.
        AgeWidgetProvider.updateAll(this);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
