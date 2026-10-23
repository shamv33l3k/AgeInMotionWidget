package dev.ageinmotion.compat;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView liveAge;
    private TextView birthDateLabel;
    private TextView runningCaption;
    private boolean active;

    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            if (!active) return;
            LocalDate dob = AgeCalculator.birthday(MainActivity.this);
            liveAge.setImageBitmap(DotMatrix.bitmap(AgeCalculator.decimalAge(dob, 8)));
            handler.postDelayed(this, 300L);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(8, 8, 8));
        getWindow().setNavigationBarColor(Color.rgb(8, 8, 8));

        int outer = dp(22);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(8, 8, 8));
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(outer, dp(32), outer, dp(28));

        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets system = insets.getInsets(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                scroll.setPadding(outer, dp(24) + system.top, outer, dp(28) + system.bottom);
                return insets;
            });
        }

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(body);

        TextView eyebrow = label("N O T H I N G  /  I N S P I R E D", 11, 0xFFAAAAAA);
        body.addView(eyebrow);

        TextView heading = label("AGE IN MOTION", 29, Color.WHITE);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setPadding(0, dp(15), 0, dp(8));
        body.addView(heading);

        TextView intro = label("Your life, counted in moments. Compatible with standard Android widgets on Nothing OS 3.2.",
                14, 0xFFB5B5B5);
        intro.setLineSpacing(dp(3), 1f);
        body.addView(intro);

        LinearLayout widgetCard = new LinearLayout(this);
        widgetCard.setOrientation(LinearLayout.VERTICAL);
        widgetCard.setGravity(Gravity.CENTER_VERTICAL);
        widgetCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(0xFF191919);
        cardBackground.setCornerRadius(dp(24));
        widgetCard.setBackground(cardBackground);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, dp(184));
        cardParams.topMargin = dp(32);
        body.addView(widgetCard, cardParams);

        TextView widgetTitle = label("A G E  I N  M O T I O N", 10, 0xFFC1C1C1);
        widgetTitle.setTypeface(Typeface.MONOSPACE);
        widgetCard.addView(widgetTitle);

        liveAge = new ImageView(this);
        liveAge.setScaleType(ImageView.ScaleType.FIT_CENTER);
        widgetCard.addView(liveAge, new LinearLayout.LayoutParams(-1, 0, 1f));

        runningCaption = label("YOUR AGE / LIVE PREVIEW", 10, 0xFFA5A5A5);
        runningCaption.setTypeface(Typeface.MONOSPACE);
        widgetCard.addView(runningCaption);

        TextView chooseTitle = label("DATE OF BIRTH", 12, 0xFFCCCCCC);
        chooseTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams section = new LinearLayout.LayoutParams(-1, -2);
        section.topMargin = dp(30);
        body.addView(chooseTitle, section);

        birthDateLabel = label("Not set", 20, Color.WHITE);
        birthDateLabel.setPadding(0, dp(10), 0, dp(14));
        body.addView(birthDateLabel);

        Button select = new Button(this);
        select.setText("CHOOSE MY BIRTHDAY");
        select.setAllCaps(false);
        select.setTextColor(Color.BLACK);
        select.setTextSize(14);
        select.setBackground(rounded(0xFFFFFFFF, dp(15)));
        select.setOnClickListener(v -> showDatePicker());
        body.addView(select, new LinearLayout.LayoutParams(-1, dp(53)));

        Button forceRefresh = new Button(this);
        forceRefresh.setText("REFRESH HOME-SCREEN WIDGET");
        forceRefresh.setAllCaps(false);
        forceRefresh.setTextColor(Color.WHITE);
        forceRefresh.setTextSize(12);
        forceRefresh.setBackground(rounded(0xFF2C2C2C, dp(15)));
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(-1, dp(51));
        refreshParams.topMargin = dp(12);
        body.addView(forceRefresh, refreshParams);
        forceRefresh.setOnClickListener(v -> {
            AgeWidgetProvider.updateAll(this);
            android.widget.Toast.makeText(this, "Widget refreshed", android.widget.Toast.LENGTH_SHORT).show();
        });

        TextView steps = label(
                "ADD TO YOUR HOME SCREEN\n\n"
              + "1. Set your date of birth above.\n"
              + "2. Long-press an empty area of your home screen.\n"
              + "3. Tap Widgets and find Age in Motion.\n"
              + "4. Drag it onto the home screen (2 × 2 recommended).\n\n"
              + "The app preview counts continuously. On the home screen, the age number refreshes "
              + "approximately every 30 minutes, while the time-today seconds keep ticking. "
              + "Android manages widget refreshes to save battery.",
                13, 0xFFAEAEAE);
        steps.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams stepsParams = new LinearLayout.LayoutParams(-1, -2);
        stepsParams.topMargin = dp(30);
        body.addView(steps, stepsParams);

        setContentView(scroll);
        updateBirthdayLabel();
    }

    @Override protected void onResume() {
        super.onResume();
        active = true;
        handler.post(refresh);
        updateBirthdayLabel();
    }

    @Override protected void onPause() {
        active = false;
        handler.removeCallbacks(refresh);
        super.onPause();
    }

    private void showDatePicker() {
        LocalDate initial = AgeCalculator.birthday(this);
        if (initial == null) initial = LocalDate.now().minusYears(23);
        DatePickerDialog picker = new DatePickerDialog(this, (DatePicker view, int year, int month, int day) -> {
            AgeCalculator.saveBirthday(this, LocalDate.of(year, month + 1, day));
            updateBirthdayLabel();
            AgeWidgetProvider.updateAll(this);
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.getDatePicker().setMinDate(LocalDate.of(1900, 1, 1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        picker.show();
    }

    private void updateBirthdayLabel() {
        if (birthDateLabel == null) return;
        LocalDate birthday = AgeCalculator.birthday(this);
        if (birthday == null) {
            birthDateLabel.setText("Not set — tap below");
            runningCaption.setText("SET YOUR BIRTHDAY TO START");
        } else {
            birthDateLabel.setText(birthday.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US)));
            runningCaption.setText("YOUR AGE / LIVE PREVIEW");
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String text, int sp, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        return tv;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }
}
