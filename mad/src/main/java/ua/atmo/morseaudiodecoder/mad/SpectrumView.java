package ua.atmo.morseaudiodecoder.mad;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.View;

public class SpectrumView extends View {
    int width;
    int height;
    int[] colormap = {Color.BLUE, Color.RED};
    Paint paint;

    @SuppressWarnings("deprecation")
    public SpectrumView(Context context) {
        super(context);
        Display display = ((MainActivity)context).getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = (int)(display.getHeight()*0.6);
        paint = new Paint();

        DrawingTask drawingTask = new DrawingTask(this);
        drawingTask.setRunning(true);
        drawingTask.execute();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int max = 100;
        int nrect = 100, h = 5, step = max/nrect;
        for (int i = 0; i<nrect; ++i) {
            int color = getColor(i*step, max+1);
            paint.setARGB(255, Color.red(color), Color.green(color), Color.blue(color));
            canvas.drawRect(0, i*h, width, (i+1)*h, paint);
        }
    }

    private int getColor(int value, int max) {
        int step = max/(colormap.length-1);
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
