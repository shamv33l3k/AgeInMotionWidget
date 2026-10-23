package dev.ageinmotion.compat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

/** Original 5x7 dot glyphs rendered programmatically; no external fonts or assets. */
final class DotMatrix {
    private static final Map<Character, String[]> FONT = new HashMap<>();
    static {
        FONT.put('0', new String[]{"01110", "10001", "10011", "10101", "11001", "10001", "01110"});
        FONT.put('1', new String[]{"00100", "01100", "00100", "00100", "00100", "00100", "01110"});
        FONT.put('2', new String[]{"01110", "10001", "00001", "00010", "00100", "01000", "11111"});
        FONT.put('3', new String[]{"11110", "00001", "00001", "01110", "00001", "00001", "11110"});
        FONT.put('4', new String[]{"00010", "00110", "01010", "10010", "11111", "00010", "00010"});
        FONT.put('5', new String[]{"11111", "10000", "10000", "11110", "00001", "00001", "11110"});
        FONT.put('6', new String[]{"01111", "10000", "10000", "11110", "10001", "10001", "01110"});
        FONT.put('7', new String[]{"11111", "00001", "00010", "00100", "00100", "00100", "00100"});
        FONT.put('8', new String[]{"01110", "10001", "10001", "01110", "10001", "10001", "01110"});
        FONT.put('9', new String[]{"01110", "10001", "10001", "01111", "00001", "00010", "11100"});
        FONT.put('.', new String[]{"00000", "00000", "00000", "00000", "00000", "01100", "01100"});
        FONT.put('-', new String[]{"00000", "00000", "00000", "11111", "00000", "00000", "00000"});
    }

    private DotMatrix() { }

    static Bitmap bitmap(String content) {
        int width = 900;
        int height = 170;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        int cols = content.length() * 6 - 1;
        // Equal spacing in both axes preserves perfectly round dot pixels.
        float pitch = Math.min((width - 36f) / Math.max(1, cols), (height - 24f) / 7f);
        float startX = (width - cols * pitch) / 2f + pitch / 2f;
        float startY = (height - 7 * pitch) / 2f + pitch / 2f;
        // Slightly fuller dots remain legible when 11 age characters are
        // scaled down to a 2x2 Android home-screen widget.
        float radius = pitch * .29f;

        for (int i = 0; i < content.length(); i++) {
            String[] glyph = FONT.get(content.charAt(i));
            if (glyph == null) continue;
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 5; col++) {
                    if (glyph[row].charAt(col) == '1') {
                        float x = startX + (i * 6 + col) * pitch;
                        float y = startY + row * pitch;
                        canvas.drawCircle(x, y, radius, paint);
                    }
                }
            }
        }
        return bitmap;
    }
}
