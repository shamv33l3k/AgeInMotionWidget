package dev.ageinmotion.compat;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

/** Full years plus fraction elapsed between birthdays, in the phone's time zone. */
final class AgeCalculator {
    private AgeCalculator() { }

    static final class AgeSnapshot {
        final int fullYears;
        final double fraction;
        final double decimalYears;
        AgeSnapshot(int years, double fractional) {
            fullYears = years;
            fraction = fractional;
            decimalYears = years + fractional;
        }
        String fractionalDigits(int digits) {
            // Truncate, don't round into 1.00000000 just before a birthday.
            long scale = 1;
            for (int i = 0; i < digits; i++) scale *= 10;
            long raw = (long) Math.floor(Math.max(0, Math.min(.99999999999, fraction)) * scale);
            return "." + String.format(Locale.US, "%0" + digits + "d", raw);
        }
        String explanation() {
            return String.format(Locale.US, "%d full years + %.2f%% toward %d",
                    fullYears, fraction * 100.0, fullYears + 1);
        }
    }

    static SharedPreferences settings(Context c) {
        return c.getSharedPreferences("age_settings", Context.MODE_PRIVATE);
    }

    static LocalDate birthday(Context context) {
        SharedPreferences p = settings(context);
        int year = p.getInt("year", 0);
        if (year == 0) return null;
        try {
            return LocalDate.of(year, p.getInt("month", 1), p.getInt("day", 1));
        } catch (RuntimeException invalid) { return null; }
    }

    static void saveBirthday(Context context, LocalDate birthday) {
        settings(context).edit()
                .putInt("year", birthday.getYear())
                .putInt("month", birthday.getMonthValue())
                .putInt("day", birthday.getDayOfMonth()).apply();
    }

    static AgeSnapshot snapshot(LocalDate birthday) {
        return snapshot(birthday, ZonedDateTime.now());
    }

    static AgeSnapshot snapshot(LocalDate birthday, ZonedDateTime now) {
        if (birthday == null || birthday.isAfter(now.toLocalDate())) return new AgeSnapshot(0, 0);
        int years = now.getYear() - birthday.getYear();
        LocalDate last = birthday.plusYears(years);
        if (last.isAfter(now.toLocalDate())) {
            years--;
            last = birthday.plusYears(years);
        }
        LocalDate next = birthday.plusYears(years + 1);
        ZoneId zone = now.getZone();
        Instant start = last.atStartOfDay(zone).toInstant();
        Instant end = next.atStartOfDay(zone).toInstant();
        double elapsed = now.toInstant().toEpochMilli() - start.toEpochMilli();
        double duration = end.toEpochMilli() - start.toEpochMilli();
        double fraction = Math.min(0.99999999999, Math.max(0.0, elapsed / duration));
        return new AgeSnapshot(years, fraction);
    }

    static String decimalAge(LocalDate birthday, int decimals) {
        if (birthday == null) return "--.--------";
        AgeSnapshot s = snapshot(birthday);
        return s.fullYears + s.fractionalDigits(decimals);
    }
}
