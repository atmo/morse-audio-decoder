package ua.atmo.morseaudiodecoder.mad;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BitmapSpectrum {
    private Bitmap bitmap;
    private int[] colormap = {Color.BLUE, Color.RED};
    private final int MAX = 100000000, STEP = 100;
    private int[] colorValues = new int[MAX / STEP];
    private int width, height;
    private int drawPosition;
    private int windowDisplayWidth;
    private Canvas canvas;
    private Paint paint;

    public BitmapSpectrum(int width, int height, int windowDisplayWidth) {
        drawPosition = 0;
        this.width = width;
        this.height = height;
        this.windowDisplayWidth = windowDisplayWidth;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        canvas = new Canvas(bitmap);
        paint = new Paint();

        for (int v = 0; v < MAX; v += STEP) {
            colorValues[v / STEP] = getColor(v);
        }
    }

    public void drawWindow(int[] window) {
        int freqCount = Math.min(window.length, height);
        for (int i = 0; i<freqCount; ++i) {
            if (window[i] >= MAX)
                window[i] = MAX - 1;
            paint.setColor(colorValues[window[i] / STEP]);
            canvas.drawRect(drawPosition, i, drawPosition + windowDisplayWidth, i+1, paint);
        }
        drawPosition += windowDisplayWidth;
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
