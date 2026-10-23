package dev.ageinmotion.compat;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import java.util.Locale;

/** Manufacturer-aware presets with a user override. Does not impersonate OEM widget APIs. */
final class DeviceTheme {
    static final String[] STYLES = {"Automatic (detect my phone)", "Nothing Dot", "Pixel / Material You", "Samsung / One UI", "Material You", "Classic monochrome"};
    static final String[] MODES = {"Follow system", "Dark", "Light"};

    final String family, deviceLabel, explanation;
    final int background, card, text, muted, accent, buttonText, widgetBackground;
    final boolean dark;

    private DeviceTheme(String family, String deviceLabel, String explanation, boolean dark,
                        int background, int card, int text, int muted, int accent, int buttonText, int widgetBackground) {
        this.family = family; this.deviceLabel = deviceLabel; this.explanation = explanation;
        this.dark = dark; this.background = background; this.card = card;
        this.text = text; this.muted = muted; this.accent = accent;
        this.buttonText = buttonText; this.widgetBackground = widgetBackground;
    }

    static DeviceTheme get(Context ctx) {
        int selection = clamp(AgeCalculator.settings(ctx).getInt("theme_style", 0), STYLES.length);
        int mode = clamp(AgeCalculator.settings(ctx).getInt("theme_mode", 0), MODES.length);
        boolean systemDark = (ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        boolean dark = mode == 0 ? systemDark : mode == 1;
        String manufacturer = (Build.MANUFACTURER == null ? "" : Build.MANUFACTURER).toLowerCase(Locale.ROOT);
        String brand = (Build.BRAND == null ? "" : Build.BRAND).toLowerCase(Locale.ROOT);
        int detected;
        if (manufacturer.contains("nothing") || brand.contains("nothing")) detected = 1;
        else if (manufacturer.contains("google") || brand.contains("google")) detected = 2;
        else if (manufacturer.contains("samsung") || brand.contains("samsung")) detected = 3;
        else detected = Build.VERSION.SDK_INT >= 31 ? 4 : 5;
        int choice = selection == 0 ? detected : selection;
        String phone = safe(Build.MANUFACTURER) + " " + safe(Build.MODEL) + " • Android " + Build.VERSION.RELEASE;
        int bg, card, text, muted, accent, onAccent, widget;
        String family, why;
        switch(choice) {
            case 1:
                family="Nothing Dot"; why="Monochrome dot-matrix design (inspired by Nothing; unofficial).";
                bg=dark?0xFF090909:0xFFF5F5F5; card=dark?0xFF191919:Color.WHITE;
                text=dark?Color.WHITE:0xFF161616; muted=dark?0xFFB3B3B3:0xFF5A5A5A;
                accent=text; onAccent=bg; widget=dark?R.drawable.widget_bg_nothing_dark:R.drawable.widget_bg_nothing_light;
                break;
            case 2:
                family="Pixel / Material You"; why="Rounded Material-inspired style with Android wallpaper accent where available.";
                bg=dark?0xFF111318:0xFFF7F7FC; card=dark?0xFF24262C:0xFFEAEAF3;
                text=dark?0xFFF5F5FB:0xFF1B1B24; muted=dark?0xFFB6B6C5:0xFF515564;
                accent=systemAccent(ctx,dark?0xFFB5C5FF:0xFF455DAA); onAccent=dark?0xFF101420:Color.WHITE;
                widget=dark?R.drawable.widget_bg_pixel_dark:R.drawable.widget_bg_pixel_light;
                break;
            case 3:
                family="Samsung / One UI"; why="One UI-inspired soft surfaces (unofficial, not a Samsung widget).";
                bg=dark?0xFF0C1523:0xFFF7F9FF; card=dark?0xFF202D40:0xFFE8EDF8;
                text=dark?Color.WHITE:0xFF15243D; muted=dark?0xFFC1CBD9:0xFF526175;
                accent=0xFF548EFF; onAccent=Color.WHITE;
                widget=dark?R.drawable.widget_bg_samsung_dark:R.drawable.widget_bg_samsung_light;
                break;
            case 4:
                family="Material You"; why="Uses your Android system accent on Android 12+, where available.";
                bg=dark?0xFF16151B:0xFFF8F6FA; card=dark?0xFF28262E:0xFFF0EDF5;
                text=dark?0xFFF5F0F9:0xFF211D27; muted=dark?0xFFC2BAC9:0xFF675F6F;
                accent=systemAccent(ctx,dark?0xFFD3B9EF:0xFF68418B); onAccent=dark?0xFF20152B:Color.WHITE;
                widget=dark?R.drawable.widget_bg_material_dark:R.drawable.widget_bg_material_light;
                break;
            default:
                family="Classic monochrome"; why="High-contrast, compatible fallback for older Android phones.";
                bg=dark?Color.BLACK:0xFFFFFFFF; card=dark?0xFF171717:0xFFF0F0F0;
                text=dark?Color.WHITE:Color.BLACK; muted=dark?0xFFB0B0B0:0xFF555555;
                accent=text; onAccent=bg; widget=dark?R.drawable.widget_bg_nothing_dark:R.drawable.widget_bg_nothing_light;
                break;
        }
        return new DeviceTheme(family, phone, why, dark, bg, card, text, muted, accent, onAccent, widget);
    }

    private static int systemAccent(Context ctx, int fallback) {
        if (Build.VERSION.SDK_INT < 31) return fallback;
        try { return ctx.getColor(android.R.color.system_accent1_500); }
        catch (Exception ignored) { return fallback; }
    }
    private static String safe(String s) { return s == null ? "Unknown" : s.trim(); }
    private static int clamp(int value, int length) { return value >= 0 && value < length ? value : 0; }
}
