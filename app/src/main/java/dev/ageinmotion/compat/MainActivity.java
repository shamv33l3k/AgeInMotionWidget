package dev.ageinmotion.compat;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
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
    private ImageView wholeImage;
    private ImageView fractionImage;
    private TextView birthDateLabel;
    private TextView liveExplanation;
    private Button motionButton;
    private boolean active;
    private DeviceTheme palette;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 2002;

    private final Runnable redraw = new Runnable() {
        @Override public void run() {
            if (!active) return;
            LocalDate dob = AgeCalculator.birthday(MainActivity.this);
            AgeCalculator.AgeSnapshot s = AgeCalculator.snapshot(dob);
            wholeImage.setImageBitmap(DotMatrix.bitmap(dob == null ? "--" :
                    String.valueOf(s.fullYears), 180, 108, palette.accent));
            fractionImage.setImageBitmap(DotMatrix.bitmap(dob == null ? ".--------" :
                    s.fractionalDigits(8), 490, 108, palette.accent));
            liveExplanation.setText(dob == null ? "Choose your birthday to begin." :
                    s.fullYears + " = completed birthdays\n" +
                    s.fractionalDigits(8) + " = part of the year since your last birthday\n" +
                    String.format(Locale.US, "%.2f%% of the way to age %d", s.fraction * 100.0, s.fullYears + 1));
            handler.postDelayed(this, 400L);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        palette = DeviceTheme.get(this);
        getWindow().setStatusBarColor(palette.background);
        getWindow().setNavigationBarColor(palette.background);
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().getDecorView().setSystemUiVisibility(palette.dark ? 0 :
                    (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));
        }
        int pad = dp(22);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(palette.background);
        scroll.setFillViewport(true);
        scroll.setPadding(pad, dp(34), pad, dp(28));
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                scroll.setPadding(pad, dp(28) + bars.top, pad, dp(28) + bars.bottom);
                return insets;
            });
        }
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(body);

        body.addView(label("U N I V E R S A L  /  A N D R O I D  W I D G E T", 10, palette.muted));
        TextView title = label("AGE IN MOTION", 28, palette.text);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(0, dp(12), 0, dp(8));
        body.addView(title);
        TextView intro = label("Every second matters. See your exact age, explained — with battery-aware motion.",
                14, palette.muted);
        intro.setPadding(0, 0, 0, dp(24));
        body.addView(intro);

        // Device manufacturer detection never locks the user into a brand theme.
        body.addView(label("YOUR DEVICE", 11, palette.muted));
        TextView device = label(palette.deviceLabel, 14, palette.text);
        device.setPadding(0, dp(6), 0, dp(6));
        body.addView(device);
        TextView explanation = label(palette.family + " • " + palette.explanation, 12, palette.muted);
        explanation.setPadding(0, 0, 0, dp(10));
        body.addView(explanation);
        body.addView(label("WIDGET THEME", 11, palette.text));
        Spinner themePicker = styledSpinner(DeviceTheme.STYLES,
                AgeCalculator.settings(this).getInt("theme_style", 0));
        body.addView(themePicker);
        themePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (AgeCalculator.settings(MainActivity.this).getInt("theme_style", 0) != pos) {
                    AgeCalculator.settings(MainActivity.this).edit().putInt("theme_style", pos).apply();
                    AgeWidgetProvider.updateAll(MainActivity.this);
                    recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        body.addView(label("APPEARANCE", 11, palette.text));
        Spinner modePicker = styledSpinner(DeviceTheme.MODES,
                AgeCalculator.settings(this).getInt("theme_mode", 0));
        body.addView(modePicker);
        modePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (AgeCalculator.settings(MainActivity.this).getInt("theme_mode", 0) != pos) {
                    AgeCalculator.settings(MainActivity.this).edit().putInt("theme_mode", pos).apply();
                    AgeWidgetProvider.updateAll(MainActivity.this);
                    recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setPadding(dp(18), dp(20), dp(18), dp(18));
        preview.setBackground(rounded(palette.card, dp(24)));
        body.addView(preview, new LinearLayout.LayoutParams(-1, dp(212)));
        preview.addView(label("A G E   I N   M O T I O N", 11, palette.text));
        LinearLayout digits = new LinearLayout(this);
        digits.setOrientation(LinearLayout.HORIZONTAL);
        digits.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams digitParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        preview.addView(digits, digitParams);
        wholeImage = new ImageView(this);
        fractionImage = new ImageView(this);
        wholeImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        fractionImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        digits.addView(wholeImage, new LinearLayout.LayoutParams(0, -1, 3));
        digits.addView(fractionImage, new LinearLayout.LayoutParams(0, -1, 7));
        LinearLayout hints = new LinearLayout(this);
        hints.addView(label("FULL YEARS", 10, palette.muted), new LinearLayout.LayoutParams(0, -2, 3));
        hints.addView(label("FRACTION OF THIS YEAR", 10, palette.muted), new LinearLayout.LayoutParams(0, -2, 7));
        preview.addView(hints);
        liveExplanation = label("Choose your birthday to begin.", 12, palette.text);
        liveExplanation.setPadding(0, dp(15), 0, dp(18));
        liveExplanation.setLineSpacing(dp(3), 1f);
        body.addView(liveExplanation);

        TextView dobTitle = label("DATE OF BIRTH", 12, palette.muted);
        dobTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        body.addView(dobTitle);
        birthDateLabel = label("Not set", 19, palette.text);
        birthDateLabel.setPadding(0, dp(10), 0, dp(11));
        body.addView(birthDateLabel);
        Button birthdayButton = new Button(this);
        birthdayButton.setText("SET MY BIRTHDAY");
        styleButton(birthdayButton, true);
        birthdayButton.setOnClickListener(v -> chooseBirthday());
        body.addView(birthdayButton, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView motionTitle = label("SMART MOTION  /  LOW BATTERY IMPACT", 12, palette.text);
        motionTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams mh = new LinearLayout.LayoutParams(-1, -2);
        mh.topMargin = dp(25);
        body.addView(motionTitle, mh);
        TextView motionDescription = label(
                "After you unlock: age digits refresh every 2 seconds for 90 seconds, " +
                "then every 10 seconds. After 5 minutes updates pause until the next unlock. " +
                "Battery Saver slows them to 15 seconds. The service never wakes the screen.",
                12, palette.muted);
        motionDescription.setPadding(0, dp(8), 0, dp(12));
        motionDescription.setLineSpacing(dp(3), 1f);
        body.addView(motionDescription);
        motionButton = new Button(this);
        styleButton(motionButton, false);
        motionButton.setOnClickListener(v -> toggleMotion());
        body.addView(motionButton, new LinearLayout.LayoutParams(-1, dp(53)));

        CheckBox lockNotification = new CheckBox(this);
        lockNotification.setText("Show my age in a lock-screen notification");
        lockNotification.setTextColor(palette.text);
        lockNotification.setTextSize(13);
        lockNotification.setButtonTintList(android.content.res.ColorStateList.valueOf(palette.accent));
        lockNotification.setPadding(0, dp(14), 0, 0);
        lockNotification.setChecked(AgeCalculator.settings(this).getBoolean("lock_notification", false));
        lockNotification.setOnCheckedChangeListener((button, checked) -> {
            AgeCalculator.settings(this).edit().putBoolean("lock_notification", checked).apply();
            if (LiveMotionService.isRunning()) {
                startService(new Intent(this, LiveMotionService.class)
                        .setAction(LiveMotionService.ACTION_UPDATE_NOTIFICATION));
            }
        });
        body.addView(lockNotification);
        TextView lockNote = label(
                "IMPORTANT: Nothing OS 3.2 does not host third-party widgets on its native " +
                "lock screen. This optional notification is NOT a lock-screen widget. " +
                "It can appear there only if Nothing OS notification privacy settings allow it. " +
                "Leave it off if you prefer to keep your age private.",
                12, palette.muted);
        lockNote.setLineSpacing(dp(3), 1f);
        body.addView(lockNote);

        Button refresh = new Button(this);
        refresh.setText("REFRESH HOME WIDGET NOW");
        styleButton(refresh, false);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(50));
        rp.topMargin = dp(18);
        body.addView(refresh, rp);
        refresh.setOnClickListener(v -> {
            AgeWidgetProvider.updateAll(this);
            android.widget.Toast.makeText(this, "Widget refreshed", android.widget.Toast.LENGTH_SHORT).show();
        });
        TextView instructions = label(
                "HOW TO USE\n" +
                "1  Set your birthday.\n" +
                "2  Long-press your HOME SCREEN → Widgets → Age in Motion.\n" +
                "3  Use a 4 × 2 layout for readable digits.\n" +
                "4  Enable Smart Motion above to animate the decimals.\n\n" +
                "The two numbers mean: [full years] + [fraction of the year]. " +
                "Example: 35.83158642 = 35 completed years + 83.16% of the way to 36. " +
                "The decimals are NOT months, days, or seconds.\n\n" +
                "Note: Android does not provide an unprivileged way to detect precisely " +
                "when the home launcher is visible. Updates happen briefly when the " +
                "phone is unlocked, including when another app is open, before auto-pausing.\n\n" +
                "Compatible with standard home-screen widget hosts on Android 8+. " +
                "Lock-screen widgets are controlled by the device/launcher and are not guaranteed.\n\n" +
                "Original concept: Age in Motion by Rahul, Nothing Playground. " +
                "This is an unofficial recreation.", 12, palette.muted);
        instructions.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.topMargin = dp(27);
        body.addView(instructions, ip);
        setContentView(scroll);
        updateDobLabel();
        updateMotionButton();
    }

    @Override protected void onResume() {
        super.onResume();
        active = true;
        handler.post(redraw);
        updateDobLabel();
        updateMotionButton();
    }
    @Override protected void onPause() {
        active = false;
        handler.removeCallbacks(redraw);
        super.onPause();
    }

    private void toggleMotion() {
        if (LiveMotionService.isRunning()) {
            stopService(new Intent(this, LiveMotionService.class));
            handler.postDelayed(this::updateMotionButton, 300L);
            return;
        }
        if (AgeCalculator.birthday(this) == null) {
            android.widget.Toast.makeText(this, "Choose your birthday first", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
            return;
        }
        startMotionService();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                      int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) startMotionService();
    }

    private void startMotionService() {
        try {
            Intent i = new Intent(this, LiveMotionService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
            else startService(i);
            handler.postDelayed(this::updateMotionButton, 400L);
        } catch (RuntimeException ex) {
            android.widget.Toast.makeText(this,
                    "Android blocked animation service; check battery and notification settings.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void updateMotionButton() {
        if (motionButton == null) return;
        boolean running = LiveMotionService.isRunning();
        motionButton.setText(running ? "●  SMART MOTION ON  —  TAP TO STOP" :
                "○  ENABLE SMART MOTION");
        styleButton(motionButton, running);
    }

    private void chooseBirthday() {
        LocalDate initial = AgeCalculator.birthday(this);
        if (initial == null) initial = LocalDate.now().minusYears(25);
        DatePickerDialog picker = new DatePickerDialog(this, (DatePicker dp, int year, int month, int day) -> {
            AgeCalculator.saveBirthday(this, LocalDate.of(year, month + 1, day));
            updateDobLabel();
            AgeWidgetProvider.updateAll(this);
            if (LiveMotionService.isRunning()) {
                startService(new Intent(this, LiveMotionService.class)
                        .setAction(LiveMotionService.ACTION_UPDATE_NOTIFICATION));
            }
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.getDatePicker().setMinDate(LocalDate.of(1900, 1, 1)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        picker.show();
    }

    private void updateDobLabel() {
        if (birthDateLabel == null) return;
        LocalDate birthday = AgeCalculator.birthday(this);
        birthDateLabel.setText(birthday == null ? "Not set — choose below" :
                birthday.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US)));
    }

    private Spinner styledSpinner(String[] values, int selected) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, values) {
            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView text = new TextView(MainActivity.this);
                text.setText(getItem(position));
                text.setTextColor(palette.text);
                text.setGravity(Gravity.CENTER_VERTICAL);
                text.setTextSize(14);
                text.setPadding(dp(15), 0, dp(8), 0);
                return text;
            }
            @Override public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                TextView text = new TextView(MainActivity.this);
                text.setText(getItem(position));
                text.setTextColor(palette.text);
                text.setBackgroundColor(palette.card);
                text.setTextSize(15);
                text.setPadding(dp(18), dp(13), dp(18), dp(13));
                return text;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setSelection(selected >= 0 && selected < values.length ? selected : 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(48));
        params.bottomMargin = dp(12);
        spinner.setLayoutParams(params);
        spinner.setBackground(rounded(palette.card, dp(12)));
        return spinner;
    }

    private void styleButton(Button b, boolean primary) {
        b.setAllCaps(false);
        b.setTextColor(primary ? palette.buttonText : palette.text);
        b.setTextSize(13);
        b.setBackground(rounded(primary ? palette.accent : palette.card, dp(14)));
    }
    private GradientDrawable rounded(int color, int corner) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(corner);
        return shape;
    }
    private TextView label(String text, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        return view;
    }
    private int dp(int amount) {
        return (int)(amount * getResources().getDisplayMetrics().density + .5f);
    }
}
