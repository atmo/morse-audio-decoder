package ua.atmo.morseaudiodecoder.mad;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;

public class SpectrumView extends View {
    private final String TAG = "SpectrumView";
    private BitmapSpectrum spectrum;
    private int width, height;

    Rect src = new Rect(), dst = new Rect();

    @SuppressWarnings("deprecation")
    public SpectrumView(Context context) {
        super(context);
        setWillNotDraw(false);
        Display display = ((MainActivity)context).getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();

        DrawingTask drawingTask = new DrawingTask(this);
        drawingTask.setRunning(true);
        drawingTask.execute();

        int windowDisplayWidth = drawingTask.getWindowDisplayWidth();
        spectrum = new BitmapSpectrum(width, height, windowDisplayWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int drawPosition = spectrum.getDrawPosition();

        src.set(0, 0, drawPosition, height);
        dst.set(width - drawPosition, 0, drawPosition, height);
        canvas.drawBitmap(spectrum.getBitmap(), src, dst, null);

        src.set(drawPosition, 0, width, height);
        dst.set(0, 0, drawPosition, height);
        canvas.drawBitmap(spectrum.getBitmap(), src, dst, null);
    }

    public void drawWindow(int[] window) {
        spectrum.drawWindow(window);
        this.invalidate();
    }
}
