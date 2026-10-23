package dev.ageinmotion.compat;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

/** Uses calendar anniversaries, not a fixed 365-day approximation. */
final class AgeCalculator {
    private AgeCalculator() { }

    static LocalDate birthday(Context context) {
        SharedPreferences p = context.getSharedPreferences("age_settings", Context.MODE_PRIVATE);
        int year = p.getInt("year", 0);
        if (year == 0) return null;
        try {
            return LocalDate.of(year, p.getInt("month", 1), p.getInt("day", 1));
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    static void saveBirthday(Context context, LocalDate birthday) {
        context.getSharedPreferences("age_settings", Context.MODE_PRIVATE).edit()
                .putInt("year", birthday.getYear())
                .putInt("month", birthday.getMonthValue())
                .putInt("day", birthday.getDayOfMonth())
                .apply();
    }

    static String decimalAge(LocalDate birthday, int decimals) {
        if (birthday == null) return decimals == 4 ? "00.0000" : "00.00000000";
        double value = calculate(birthday, ZonedDateTime.now());
        return String.format(Locale.US, "%1$." + decimals + "f", value);
    }

    static double calculate(LocalDate birthday, ZonedDateTime now) {
        ZoneId zone = now.getZone();
        LocalDate today = now.toLocalDate();
        if (birthday.isAfter(today)) return 0.0;

        int years = today.getYear() - birthday.getYear();
        LocalDate lastBirthday = birthday.plusYears(years);
        if (lastBirthday.isAfter(today)) {
            years--;
            lastBirthday = birthday.plusYears(years);
        }
        LocalDate nextBirthday = birthday.plusYears(years + 1);
        Instant start = lastBirthday.atStartOfDay(zone).toInstant();
        Instant end = nextBirthday.atStartOfDay(zone).toInstant();
        double elapsed = (now.toInstant().toEpochMilli() - start.toEpochMilli());
        double total = (end.toEpochMilli() - start.toEpochMilli());
        return years + Math.min(0.9999999999, Math.max(0, elapsed / total));
    }
}
