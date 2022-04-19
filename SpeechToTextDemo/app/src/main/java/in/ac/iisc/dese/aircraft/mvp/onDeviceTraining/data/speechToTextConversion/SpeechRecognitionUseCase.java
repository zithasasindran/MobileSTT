package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion;

import static in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util.Constants.RECORDING_LENGTH;
import static in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util.Constants.SAMPLE_RATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import org.tensorflow.demo.ReadExample;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextContract;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextMainActivity;

import io.reactivex.Single;
import timber.log.Timber;

public class SpeechRecognitionUseCase implements SpeechToTextContract.SpeechRecognitionUseCaseHelper {
    private static final String OUTPUT_SCORES_NAME = "output";
    short[] recordingBuffer;
    int recordingOffset = 0;
    long time1;
    long time2;
    long twbs;
    List<Future<String>> pending;
    ExecutorService executor;
    double SNR = 0;
    volatile boolean shouldContinue = true;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();

    AssetManager assetManager;
    private short[] audioBuffer;
    static ArrayList<ArrayList<Float>> finalProbablitlityMatrix;
    private AudioRecord recorder;
    private Context context;
    private int bufferSize;
    public static int r;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy HH:mm a");

    public SpeechRecognitionUseCase(Context context) {
        this.context = context;

        assetManager = context.getAssets();
        final Interpreter.Options tfLiteOptions = new Interpreter.Options();
        tfLiteOptions.setNumThreads(2);

    }

    @Override
    public Single<Boolean> initAudioRecorder() {
        return Single.fromCallable(this::setupAudioRecorder);
    }

    @Override
    public Single<Boolean> startAudioRecorder() {
        return Single.fromCallable(this::startRecorder);
    }

    @Override
    public boolean stopAudioRecorder() {
        if (shouldContinue) {
            shouldContinue = false;
            Timber.d("SpeechToTextDemo, stopAudioRecorder, shouldContinue: %s", shouldContinue);
        }

        return true;
    }

    @Override
    public Single<String[]> startRecognition() throws FileNotFoundException {
        return Single.fromCallable(() -> doSpeechRecognize());
    }

    @Override
    public Single<SpeechToTextData> createSpeechToTextDataModel(String[] convertedText) {
        String dateTime = getCurrentDateTime();
        String[] dateTimeSplit = dateTime.split(" ");
        dateTimeSplit[0] = dateTimeSplit[0].replace("-", " ");
        return Single.just(new SpeechToTextData(convertedText, dateTimeSplit[0],
                dateTimeSplit[1], View.VISIBLE));
    }

    private String getCurrentDateTime() {
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    private boolean setupAudioRecorder() {
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        // Estimate the buffer size we'll need for this device.
        bufferSize = 16000; //AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
        //AudioFormat.ENCODING_PCM_16BIT);
        Timber.d("SpeechToTextDemo, SpeechRecognition buffer size" + bufferSize);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        Timber.d("SpeechToTextDemo, SpeechRecognition buffer size" + bufferSize);

        //audioBuffer = new short[bufferSize/2];
        audioBuffer = new short[bufferSize];
        Timber.d("SpeechToTextDemo, SpeechRecognition audio buffer size " + audioBuffer.length);

        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Timber.e("SpeechToTextDemo, SpeechRecognition, Audio Record can't initialize!");
            return false;
        } else {
            return true;
        }
    }

    private boolean startRecorder() throws IOException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        if (SpeechToTextMainActivity.isInference()) {


            //Reading from a file
            File audDir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)), "mu");
            ReadExample readExample = new ReadExample();
//        double[] audio= readExample.readaudio(new File(audDir,"1.wav"));
//        short[] audio_short=new short[audio.length];
//        int count=0;
//        for(double a: audio)
//        {
//            audio_short[count++]= (short) (a*32768);
//        }


            setupAudioRecorder();
            executor = Executors.newFixedThreadPool(1);
            pending = new ArrayList<Future<String>>();
            finalProbablitlityMatrix = new ArrayList<>();


            shouldContinue = true;
            recordingBuffer = new short[RECORDING_LENGTH];


            time1 = System.currentTimeMillis();

            recorder.startRecording();

            Timber.d("SpeechToTextDemo, SpeechRecognition, startRecorder(), Start recording");

            while (shouldContinue) {
                int numberRead = recorder.read(audioBuffer, 0, audioBuffer.length);
                //Timber.d("SpeechToTextDemo, SpeechRecognition, read: %s", numberRead);
                Timber.d("SpeechToTextDemo, SpeechRecognition, startRecorder()" + ++r);
                Timber.d("audiobuffer length" + audioBuffer.length);

                int maxLength = recordingBuffer.length;
                recordingBufferLock.lock();
                try {
                    if (recordingOffset + numberRead < maxLength) {
                        System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberRead);


                        if (r % 3 == 0) {
                            Timber.d("SpeechToTextDemo, SpeechRecognition, startRecorder() calling recognizr");
                            int time = r - 3;
                            short[] temp_record = new short[3 * 16000];
                            System.arraycopy(recordingBuffer, time * 16000, temp_record, 0, 3 * 16000);
                            //System.arraycopy(audio_short,time*16000,temp_record,0,3*16000);
                        }


                    } else {
                        shouldContinue = false;

                        Timber.d("SpeechToTextDemo, SpeechRecognition, buffer full");
                    }
                    recordingOffset += numberRead;
                    Timber.d("SpeechToTextDemo, SpeechRecognition, recording offset" + recordingOffset);
                } finally {
                    recordingBufferLock.unlock();
                }
            }
            recorder.stop();
            recorder.release();

            SNR = calculatePeakAndRms(audioBuffer);
            Timber.d("Recording complete");
        } else {
            setupAudioRecorder();
            shouldContinue = true;
            int time_to_record = SpeechToTextMainActivity.getDuration();
            recordingBuffer = new short[RECORDING_LENGTH];

            recorder.startRecording();

            Timber.d("SpeechToTextDemo, SpeechRecognition, startRecorder(), Start recording");

            while (shouldContinue) {
                int numberRead = recorder.read(audioBuffer, 0, audioBuffer.length);
                //Timber.d("SpeechToTextDemo, SpeechRecognition, read: %s", numberRead);
                Timber.d("SpeechToTextDemo, SpeechRecognition, startRecorder()" + ++r);
                Timber.d("audiobuffer length" + audioBuffer.length);

                int maxLength = recordingBuffer.length;
                recordingBufferLock.lock();
                try {
                    if (recordingOffset + numberRead < maxLength) {
                        System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberRead);

                    } else {
                        shouldContinue = false;
                        Timber.d("SpeechToTextDemo, SpeechRecognition, buffer full");
                    }
                    recordingOffset += numberRead;
                    Timber.d("SpeechToTextDemo, SpeechRecognition, recording offset" + recordingOffset);
                } finally {
                    recordingBufferLock.unlock();
                }
            }
            recorder.stop();
            recorder.release();

            SNR = calculatePeakAndRms(audioBuffer);
            Timber.d("Recording complete");
        }
        return true;
    }

    public static double calculatePeakAndRms(short[] samples) {
        double sumOfSampleSq = 0.0;    // sum of square of normalized samples.
        double peakSample = 0.0;     // peak sample.

        for (double sample : samples) {
            double normSample = (double) sample / 32767;  // normalized the sample with maximum value.
            sumOfSampleSq += (normSample * normSample);
            if (Math.abs(sample) > peakSample) {
                peakSample = Math.abs(sample);
            }
        }

        double rms = 10 * Math.log10(sumOfSampleSq / samples.length);
        double peak = 20 * Math.log10(peakSample / 32767);
        return Math.abs(rms / 2);
    }

    private String[] doSpeechRecognize() throws FileNotFoundException {
        if (SpeechToTextMainActivity.isInference()) {
            Log.d("r values is", "dd" + String.valueOf(r));
            String result_string = "";
            for (Future<String> result : pending) {
                try {
                    result_string = result_string + result.get() + " ";
                    System.out.println("Your result ASAP:" + result.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
            String[] output = new String[3];


            output[0] = "OUTPUT AS IS:\n\n " + result_string;
            Long aftercpp = System.currentTimeMillis();

            writeFile("", "", String.valueOf(twbs));


            Timber.d("OUTPUT AS IS =========> " + output + "\n\n\n\n\n");
            Timber.d("OUTPUT  =========> " + result_string + "\n\n\n\n\n");


            time2 = 0;
            time1 = 0;
            r = 0;
            recordingOffset = 0;


            SpeechToTextMainActivity.setMessage("", "");
            return output;
        } else {
            finalProbablitlityMatrix = new ArrayList<>();


            int record_length = SAMPLE_RATE * r;
            Log.d("record length", String.valueOf(record_length));

            short[] inputBuffer = new short[RECORDING_LENGTH];
            double[] doubleInputBuffer = new double[recordingOffset];

            String[] outputScoresNames = new String[]{OUTPUT_SCORES_NAME};

            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                System.arraycopy(recordingBuffer, 0, inputBuffer, 0, RECORDING_LENGTH);
            } finally {
                recordingBufferLock.unlock();
            }

            String filename = Environment.DIRECTORY_MOVIES;
            Log.w("inputbuffer", String.valueOf(inputBuffer.length));
            double[] audio = new double[r * SAMPLE_RATE];
            int count = 0;
            for (short i : inputBuffer) {
                if (count < r * SAMPLE_RATE) {
                    audio[count++] = i / 32768.0;
                }

            }


            SpeechToTextMainActivity.present(audio);


            r = 0;
            recordingOffset = 0;
            return new String[3];
        }
    }


    private static double SPL(double[] audio) {
        double[] sq_audio = new double[audio.length];
        double mean = 0;
        for (int i = 0; i < audio.length; i++) {
            sq_audio[i] = Math.pow(audio[i], 2);
            mean = mean + sq_audio[i];
        }
        mean = mean / audio.length;
        double p_rms = Math.pow(mean, 0.5);
        double SPL = 20 * Math.log10((p_rms / (2e-5)));

        return SPL;
    }


    public void writeFile(String mValue, String nvalue, String kValue) {

        try {
            String filename = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/output_test.txt";
            Log.w("filename", filename);
            FileWriter fw = new FileWriter(filename, true);
            fw.write(mValue + ";" + nvalue + ";" + kValue);
            fw.write("\n");
            fw.close();
        } catch (IOException ioe) {
        }

    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }




}