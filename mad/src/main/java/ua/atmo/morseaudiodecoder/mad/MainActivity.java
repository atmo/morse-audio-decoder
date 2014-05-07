package ua.atmo.morseaudiodecoder.mad;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class MainActivity extends ActionBarActivity {
    SpectrumView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new SpectrumView(this);
        setContentView(view);
    }

}
