package ua.atmo.morseaudiodecoder.mad;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import edu.emory.mathcs.jtransforms.dct.DoubleDCT_1D;

class DrawingTask extends AsyncTask<Void, double[], Void> {
    private static final String TAG = "DRAWING_TASK";

    private AudioRecord recorder;
    private int audioBufferSize, sampleRate;
    private short[][] audioBuffers;
    private int audioBufferIdx;

    private SpectrumView view;
    private int windowDisplayWidth;

    private double[][] windowBuffers;
    private int windowBufferIdx;
    private int[] energy;
    private DoubleDCT_1D transform;

    private boolean isRunning = false;
    private int windowAudioBufferPos;

    public DrawingTask(SpectrumView view) {
        recorder = findAudioRecord();
        audioBufferIdx = 0;
        windowBufferIdx = 0;
        this.view = view;

        final int AUDIO_BUFFERS_COUNT = 256;
        audioBuffers = new short[AUDIO_BUFFERS_COUNT][audioBufferSize];

        final int MINIMAL_WINDOW_LENGTH = 5000;
        int windowLength = 0;
        while (windowLength<MINIMAL_WINDOW_LENGTH)
            windowLength += audioBufferSize;

        final int WINDOW_BUFFERS_COUNT = 256;
        windowBuffers = new double[WINDOW_BUFFERS_COUNT][windowLength];
        energy = new int[windowLength];

        final int DISPLAY_INTERVAL = 3;
        int samplesCount = DISPLAY_INTERVAL*sampleRate;
        windowDisplayWidth = (windowLength * windowDisplayWidth + samplesCount / 2) / samplesCount;

        transform = new DoubleDCT_1D(windowLength);
    }

    public AudioRecord findAudioRecord() {
        AudioRecord recorder = null;

        final int[] sampleRates = new int[]{44100, 22050, 11025, 8000};
        final short[] audioFormats = new short[]{AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT};
        final short[] channelConfigs = new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};

        for (int rate : sampleRates) {
            for (short encoding : audioFormats) {
                for (short channelConfig : channelConfigs) {
                    try {
                        Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + encoding + ", channel: "
                                + channelConfig);
                        int BUFFER_SIZE_BYTES = AudioRecord.getMinBufferSize(rate, channelConfig, encoding);

                        if (BUFFER_SIZE_BYTES != AudioRecord.ERROR_BAD_VALUE) {
                            if (recorder != null)
                                recorder.release();
                            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, encoding, BUFFER_SIZE_BYTES);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                Log.d(TAG, "Success with buffer size: " + BUFFER_SIZE_BYTES);
                                if (encoding == AudioFormat.ENCODING_PCM_16BIT)
                                    audioBufferSize = BUFFER_SIZE_BYTES / Short.SIZE;
                                else
                                    audioBufferSize = BUFFER_SIZE_BYTES / Byte.SIZE;
                                sampleRate = rate;
                                return recorder;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        if (recorder == null || recorder.getState() == AudioRecord.STATE_UNINITIALIZED)
            Log.e(TAG, "Uninitialized recorder!");
        return null;
    }

    public void setRunning(boolean run) {
        isRunning = run;
    }

    public int getWindowDisplayWidth() {
        return windowDisplayWidth;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            recorder.startRecording();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error starting recording:" + e);
            throw e;
        }

        short[] buffer;
        double[] window;
        while (isRunning) {
            buffer = audioBuffers[audioBufferIdx];
            audioBufferIdx = (audioBufferIdx == audioBuffers.length - 1 ? 0 : audioBufferIdx + 1);
            recorder.read(buffer, 0, buffer.length);

            window = windowBuffers[windowBufferIdx];
            for (int i = windowAudioBufferPos, j = 0; j < buffer.length; ++i, ++j) {
                window[i] = buffer[j];
            }
            windowAudioBufferPos += buffer.length;
            if (windowAudioBufferPos == window.length) {
                transform.forward(window, false);
                publishProgress(window);
                windowAudioBufferPos = 0;
                windowBufferIdx = (windowBufferIdx == windowBuffers.length - 1 ? 0 : windowBufferIdx + 1);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(double[]... values) {
        double[] freq = values[0];
        for (int i = 0; i < freq.length; ++i)
            energy[i] = (int) (freq[i] > 0 ? freq[i] : -freq[i]);
        view.drawWindow(energy);
    }
}
