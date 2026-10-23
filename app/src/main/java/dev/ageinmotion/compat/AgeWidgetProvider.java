package dev.ageinmotion.compat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.ZoneId;

public final class AgeWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int id : widgetIds) update(context, manager, id);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                          int widgetId, android.os.Bundle options) {
        super.onAppWidgetOptionsChanged(context, manager, widgetId, options);
        update(context, manager, widgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            updateAll(context);
        }
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, AgeWidgetProvider.class));
        for (int id : ids) update(context, manager, id);
    }

    private static void update(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_age);
        LocalDate birthday = AgeCalculator.birthday(context);
        String age = AgeCalculator.decimalAge(birthday, 4);
        views.setImageViewBitmap(R.id.dot_age, DotMatrix.bitmap(age));

        if (birthday == null) {
            views.setTextViewText(R.id.widget_caption, "TAP TO SET DATE OF BIRTH");
            views.setViewVisibility(R.id.live_timer, View.GONE);
        } else {
            views.setTextViewText(R.id.widget_caption, "YEARS / ALWAYS MOVING");
            views.setViewVisibility(R.id.live_timer, View.VISIBLE);
            long midnightMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            long elapsedToday = System.currentTimeMillis() - midnightMillis;
            long base = SystemClock.elapsedRealtime() - elapsedToday;
            views.setChronometer(R.id.live_timer, base, "TODAY  %s", true);
        }

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);
        manager.updateAppWidget(id, views);
    }
}
