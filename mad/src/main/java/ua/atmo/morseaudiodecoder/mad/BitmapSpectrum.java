package ua.atmo.morseaudiodecoder.mad;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BitmapSpectrum {
    private Bitmap bitmap;
    private int[] colormap = {Color.BLUE, Color.RED};
    private int width, height;
    private int drawPosition;
    private final int MAX = 1000000;
    private int windowWidth;
    private Paint paint;
    private Canvas canvas;

    public BitmapSpectrum(int width, int height, int windowWidth) {
        drawPosition = 0;
        this.width = width;
        this.height = height;
        this.windowWidth = windowWidth;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public void drawWindow(int[] window) {
        int freqCount = Math.min(window.length, height);
        for (int i = 0; i<freqCount; ++i) {
            int color = getColor(window[i]);
            paint.setARGB(255, Color.red(color), Color.green(color), Color.blue(color));
            canvas.drawRect(drawPosition, i, drawPosition + windowWidth, i+1, paint);
        }
        drawPosition += windowWidth;
        if (drawPosition >= width) {
            drawPosition = 0;
        }
    }

    public int getDrawPosition() {
        return drawPosition;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    private int getColor(int value) {
        int step = MAX/(colormap.length-1);
        if (value>=MAX)
            value = MAX-1;
        int idx = value/step;
        value -= step*idx;
        int red = interpolate(Color.red(colormap[idx]), Color.red(colormap[idx+1]), value, step)/step,
                green = interpolate(Color.green(colormap[idx]), Color.green(colormap[idx + 1]), value, step)/step,
                blue = interpolate(Color.blue(colormap[idx]), Color.blue(colormap[idx + 1]), value, step)/step;
        return Color.rgb(red, green, blue);
    }

    private int interpolate (int a, int b, int value, int max) {
        return (max - value)*a + value*b;
    }
}
