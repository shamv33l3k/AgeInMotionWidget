package dev.ageinmotion.compat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.util.Locale;

/** Standard third-party home-screen widget (Nothing lock screen hosts native widgets only). */
public final class AgeWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "dev.ageinmotion.compat.REFRESH_WIDGET";
    private static String cachedYearText = "";
    private static android.graphics.Bitmap cachedYearBitmap;

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateAll(context);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                                     int id, android.os.Bundle options) {
        super.onAppWidgetOptionsChanged(context, manager, id, options);
        updateAll(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            updateAll(context);
        }
    }

    @Override public void onDisabled(Context context) {
        context.stopService(new Intent(context, LiveMotionService.class));
        super.onDisabled(context);
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, AgeWidgetProvider.class));
        if (ids.length == 0) return;
        LocalDate dob = AgeCalculator.birthday(context);
        AgeCalculator.AgeSnapshot age = AgeCalculator.snapshot(dob);
        String whole = dob == null ? "--" : String.valueOf(age.fullYears);
        // While in Smart Motion, the LAST digits update every ~2 seconds (rather than
        // trying to redraw the entire bitmap at 30 fps and draining battery).
        String fraction = dob == null ? ".--------" : age.fractionalDigits(8);
        if (!whole.equals(cachedYearText) || cachedYearBitmap == null) {
            cachedYearText = whole;
            cachedYearBitmap = DotMatrix.bitmap(whole, 180, 108);
        }
        android.graphics.Bitmap fractionBitmap = DotMatrix.bitmap(fraction, 490, 108);
        String caption = dob == null ? "TAP TO CHOOSE YOUR BIRTHDAY"
                : String.format(Locale.US, "%.2f%% OF THE WAY TO %d",
                    age.fraction * 100.0, age.fullYears + 1);
        String mode = !LiveMotionService.isRunning() ? "ECO" :
                LiveMotionService.isAnimating() ? "LIVE" : "PAUSED";
        for (int id : ids) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_age);
            rv.setImageViewBitmap(R.id.dot_years, cachedYearBitmap);
            rv.setImageViewBitmap(R.id.dot_fraction, fractionBitmap);
            rv.setTextViewText(R.id.widget_caption, caption);
            rv.setTextViewText(R.id.widget_state, mode);
            rv.setProgressBar(R.id.year_progress, 1000, (int)(age.fraction * 1000), false);
            Intent open = new Intent(context, MainActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent openPi = PendingIntent.getActivity(context, id, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.widget_root, openPi);
            Intent refresh = new Intent(context, AgeWidgetProvider.class);
            refresh.setAction(ACTION_REFRESH);
            PendingIntent refreshPi = PendingIntent.getBroadcast(context, id, refresh,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.widget_refresh, refreshPi);
            manager.updateAppWidget(id, rv);
        }
    }
}
