package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util;

public final class Constants {

    private Constants() {
    }

    public static final int PERMISSION_RECORD_AUDIO_REQUEST_CODE = 1001;
    public static final int SAMPLE_RATE = 16000;
    public static final int SAMPLE_DURATION_MS = 20000;
    public static final int RECORDING_LENGTH = (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    //public static final String MODEL_FILENAME = "file:///android_asset/model_clean_wbs.pb";
    //public static final String MODEL_FILENAME = "file:///android_asset/model_clean_960hrs.pb";
    //public static final String MODEL_FILENAME = "file:///android_asset/model_clean_960_mel_new.pb";
    //public static final String MODEL_FILENAME = "file:///android_asset/model_clean_960_mel_new.tflite";
    //public static final String MODEL_FILENAME = "file:///android_asset/.tflite"; //supposed to be dummy tflite
    public static final String CORPUS_FILENAME = "file:///android_asset/Tensorflow_corpus.txt";
    public static final String TOKEN_FILENAME = "file:///android_asset/Tensorflow.txt";
}