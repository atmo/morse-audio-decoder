package ua.atmo.morseaudiodecoder.mad;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

class DrawingTask extends AsyncTask<Void, short[], Void>{
    private static final String TAG = "DRAWING_THREAD";

    private AudioRecord recorder;
    private int bufferSize, sampleRate;
    private final int BUFFERS_COUNT = 256;
    private short[][] audioBuffers;
    private int audioBufferIdx;

    private SpectrumView view;
    private int windowDisplayWidth;
    private double[] window;

    private boolean isRunning = false;

    public DrawingTask(SpectrumView view) {
        recorder = findAudioRecord();
        audioBufferIdx = 0;
        this.view = view;

        audioBuffers = new short[BUFFERS_COUNT][bufferSize];

        final int MINIMAL_WINDOW_LENGTH = 500;
        int windowLength = 0;
        while (windowLength<MINIMAL_WINDOW_LENGTH)
            windowLength += bufferSize;
        window = new double[windowLength];

        final int DISPLAY_INTERVAL = 3;
        int samplesCount = DISPLAY_INTERVAL*sampleRate;
        windowDisplayWidth = (window.length*windowDisplayWidth + samplesCount - 1)/samplesCount;
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
                                    bufferSize = BUFFER_SIZE_BYTES / Short.SIZE;
                                else
                                    bufferSize = BUFFER_SIZE_BYTES / Byte.SIZE;
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
        short[] buffer;
        try {
            recorder.startRecording();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error starting recording:" + e);
            throw e;
        }

        while (isRunning) {
            buffer = audioBuffers[audioBufferIdx];
            audioBufferIdx = (audioBufferIdx == BUFFERS_COUNT-1 ? 0 : audioBufferIdx +1);
            recorder.read(buffer, 0, buffer.length);
            publishProgress(buffer);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(short[]... values) {

    }
}
