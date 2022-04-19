package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;

import org.tensorflow.demo.WriteExample;
import org.tensorflow.lite.Interpreter;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.BuildConfig;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.R;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.base.BaseActivity;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util.OnSwipeTouchListener;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion.SpeechToTextData;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.animation.PulsatorLayout;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.listItem.SpeechToTextHistoryRecyclerViewAdapter;
import timber.log.Timber;

import static in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util.Constants.PERMISSION_RECORD_AUDIO_REQUEST_CODE;


public class SpeechToTextMainActivity extends BaseActivity implements SpeechToTextContract.View {

    static SpeechToTextMainActivity speechToTextMainActivity;
    static ConstraintLayout lLayout;
    static ArrayList<String> groundtruth;
    static ArrayList<String> recorded;
    static ArrayList<Integer> time;
    static AssetManager assetManager;

    static int count = 0;
    TextView message;
    public static Switch Inference;
    public static TextView textView;
    @BindView(R.id.speechHistoryRecyclerView)
    RecyclerView speechHistoryRecyclerView;

    @BindView(R.id.noDataFoundTextView)
    TextView noDataFoundTextView;

    @BindView(R.id.recordFloatingActionButton)
    FloatingActionButton recordFloatingActionButton;

    @BindView(R.id.deleteFloatingActionButton)
    FloatingActionButton deleteFloatingActionButton;


    @BindView(R.id.pulsator)
    PulsatorLayout pulsatorLayout;

    private SpeechToTextHistoryRecyclerViewAdapter speechToTextHistoryRecyclerViewAdapter;
    private final List<SpeechToTextData> speechToTextDataList = new ArrayList<>(1);
    private final String[] appPermissions = new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Inject
    SpeechToTextContract.Presenter speechToTextPresenter;

    //when train button on right is clicked training of basic tflite begins
    public void startService() {
        startService(new Intent(getBaseContext(), TrainService.class));
    }

    //initial permission requests
    @Override
    public void requestAudioRecordingPermission() {
        Timber.d("SpeechToTextDemo, Main, requestAudioRecordingPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                Timber.d("SpeechToTextDemo, Main, permission not granted");
                requestPermissions(appPermissions, PERMISSION_RECORD_AUDIO_REQUEST_CODE);
            }
        } else {
            Toast.makeText(getBaseContext(), "Tap on Mic to speak",
                    Toast.LENGTH_SHORT).show();
            speechToTextPresenter.audioRecordPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSION_RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Toast.makeText(getBaseContext(), "Please Hold while we setup the app",
                        Toast.LENGTH_SHORT).show();
                speechToTextPresenter.audioRecordPermissionGranted();

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(getBaseContext(), "You must enable the permission",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //copy groundtruth.csv and ondevicetrainingdataset.csv
    public void copyfiles() {
        try {
            File groundtruthfile = new File(Environment.getExternalStorageDirectory() + "/" + "ground_truth.csv");
            File ondevicetraincsvfile = new File(Environment.getExternalStorageDirectory() + "/" + "onDeviceTraining_dataset.csv");
            assetManager = getAssets();
            if (!groundtruthfile.exists()) {
                Train.copygroundtruth();
                SpeechToTextMainActivity.setMessage("PLEASE FILL DATASET", "");

            }
            if (!ondevicetraincsvfile.exists()) {
                Train.copyondevicetraincsv();
                System.out.println("RECREATING");
                //recreate();
            }
        } catch (Exception e) {
            System.out.println("FILES DID NOT GET COPIED");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speechToTextMainActivity = this;
        getPresentationComponent().inject(speechToTextMainActivity);
        textView = findViewById(R.id.noDataFoundTextView);
        message = findViewById(R.id.realTimeText);
        requestAudioRecordingPermission();
        copyfiles();
        FloatingActionButton train = findViewById(R.id.tempTrain);
        try {
            setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Inference = findViewById(R.id.switch1);
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("TRAIN CLICKED");
                Inference.setClickable(false);
                startService();
            }
        });
        Inference.setClickable(true);
        speechHistoryRecyclerView.setVisibility(View.INVISIBLE);
        noDataFoundTextView.setVisibility(View.VISIBLE);

        Inference.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    setup();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (b) {
                    speechHistoryRecyclerView.setVisibility(View.VISIBLE);
                    noDataFoundTextView.setVisibility(View.INVISIBLE);
                } else {
                    speechHistoryRecyclerView.setVisibility(View.INVISIBLE);
                    noDataFoundTextView.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    public static float[][] transposeMatrix(float[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;

        float[][] transposedMatrix = new float[n][m];

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                transposedMatrix[x][y] = matrix[y][x];
            }
        }

        return transposedMatrix;
    }


    @Override
    protected void setup() throws IOException {
        setUnBinder(ButterKnife.bind(this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        speechToTextHistoryRecyclerViewAdapter = new SpeechToTextHistoryRecyclerViewAdapter();
        speechHistoryRecyclerView.setLayoutManager(layoutManager);
        speechHistoryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        speechHistoryRecyclerView.setAdapter(speechToTextHistoryRecyclerViewAdapter);
        speechHistoryRecyclerView.setHasFixedSize(false);

        AssetManager assetManager = getAssets();
        //requestAudioRecordingPermission();
        groundtruth = new ArrayList<>();
        time = new ArrayList<>();
        recorded = new ArrayList<>();
        lLayout = findViewById(R.id.layout);


        try {

            CSVReader reader = new CSVReader(new FileReader(new File(Environment.getExternalStorageDirectory(), "OnDeviceTraining_dataset.csv")));

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                recorded.add(nextLine[1]);
                System.out.println(nextLine[1]);
            }
        } catch (Exception e) {

        }


        try {
            //CSVReader reader = new CSVReader(new FileReader(new File(Environment.getExternalStorageDirectory(),"ground_truth.csv")));
            CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("ground_truth.csv")));

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                groundtruth.add(nextLine[0]);
                time.add(Integer.valueOf(nextLine[1]));
                Log.w("csv", nextLine[0] + nextLine[1]);
            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            count = preferences.getInt("count", 0);

            if (recorded.contains(groundtruth.get(count))) {
                lLayout.setBackgroundColor(Color.parseColor("#F6BCD0"));
            } else {
                lLayout.setBackgroundColor(Color.parseColor("#b8d5cd"));
            }


            textView.setText((count + 1) + "/" + groundtruth.size() + "\n" + groundtruth.get(count));
            message.setText("Please start speaking as soon as you click record");

            ConstraintLayout constraintLayout = findViewById(R.id.layout);
            constraintLayout.setOnTouchListener(new OnSwipeTouchListener(SpeechToTextMainActivity.this) {

                // ADD LOGIC FOR IN ORDER ADDING DATASET

                public void onSwipeRight() {
                    if (count == 0) {
                        //count = groundtruth.size() - 1;
                    } else {
                        count = (count - 1) % groundtruth.size();
                    }
                    textView.setText((count + 1) + "/" + groundtruth.size() + "\n" + groundtruth.get(count));
                    if (recorded.contains(groundtruth.get(count))) {
                        lLayout.setBackgroundColor(Color.parseColor("#F6BCD0"));
                        recordFloatingActionButton.setClickable(false);
                    } else {
                        if (count != 0 && !recorded.contains(groundtruth.get(count - 1))) {
                            recordFloatingActionButton.setClickable(false);
                            Toast.makeText(getApplicationContext(), "Please Make Sure the previous sentence was recorded!", Toast.LENGTH_SHORT).show();
                        } else {
                            recordFloatingActionButton.setClickable(true);
                        }
                        lLayout.setBackgroundColor(Color.parseColor("#b8d5cd"));
                    }

                    editor.putInt("count", count);
                    editor.apply();

                }

                public void onSwipeLeft() {
                    count = (count + 1) % groundtruth.size();
                    textView.setText((count + 1) + "/" + groundtruth.size() + "\n" + groundtruth.get(count));
                    if (recorded.contains(groundtruth.get(count))) {
                        lLayout.setBackgroundColor(Color.parseColor("#F6BCD0"));
                        recordFloatingActionButton.setClickable(false);

                    } else {
                        if (count != 0 && !recorded.contains(groundtruth.get(count - 1))) {
                            recordFloatingActionButton.setClickable(false);
                            Toast.makeText(getApplicationContext(), "Please Make Sure the previous sentence was recorded!", Toast.LENGTH_SHORT).show();
                        } else {
                            recordFloatingActionButton.setClickable(true);
                        }
                        lLayout.setBackgroundColor(Color.parseColor("#b8d5cd"));
                    }

                    editor.putInt("count", count);
                    editor.apply();
                }


            });
        } catch (IOException | CsvValidationException e) {
            Log.w("csv", e.getMessage());

        }

    }


    @OnClick(R.id.recordFloatingActionButton)
    public void onRecordFloatingActionButtonClick(View v) {
        Timber.d("SpeechToTextDemo onRecord click: %s", v.getTag().toString());

        speechToTextPresenter.recordFabOnClick(v.getTag().toString());
    }

    @OnClick(R.id.deleteFloatingActionButton)
    public void onDeleteFloatingActionButtonClick() {
        speechToTextPresenter.deleteFabOnClick();
    }

    //@OnClick(R.id.shareFloatingActionButton)
    //public void onShareFloatingActionButtonClick() {
    //    speechToTextPresenter.shareFabOnClick();
    //}

    @Override
    public void startPulsatorViewAnimation() {
        pulsatorLayout.start();
    }

    @Override
    public void stopPulsatorViewAnimation() {
        //idealRecorder.stop();
        pulsatorLayout.stop();
    }

    @Override
    public void updateFabViewToRecordingState() {
        //enableOrDisableDeleteAndShareFloatingActionButtons(false);
        recordFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.colorAccent)));
        recordFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
    }

    @Override
    public void updateFabViewToStopState() {
        // when recorder stops, disable the recorder fab button;
        // wait until recognition finishes its job
        recordFloatingActionButton.setEnabled(false);
        recordFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.cyan_300)));
        recordFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_mic_white_24dp));
    }

    @Override
    public void setRecordButtonTag(String tag) {
        recordFloatingActionButton.setTag(tag);
    }

    @Override
    public void bindSpeechDataToRecyclerView(SpeechToTextData speechToTextData) {
        //enableOrDisableDeleteAndShareFloatingActionButtons(true);
        // re-enable recorder fab button
        recordFloatingActionButton.setEnabled(true);
        speechToTextDataList.add(speechToTextData);
        speechToTextHistoryRecyclerViewAdapter.add(adjustDateVisibility(speechToTextData));
        if (speechToTextDataList.size() > 0) {
            // hideNoDataFoundText();
        }
    }

    private SpeechToTextData adjustDateVisibility(SpeechToTextData speechToTextData) {
        String dateTime = speechToTextData.getDate();
        if (speechToTextDataList.size() == 0) {
            speechToTextData.setDateVisibilityState(View.VISIBLE);
        } else {
            SpeechToTextData lastIndexSpeechToTextData =
                    speechToTextDataList.get(speechToTextDataList.size() - 1);
            if (lastIndexSpeechToTextData.getDate().equals(dateTime)) {
                speechToTextData.setDateVisibilityState(View.GONE);
            } else {
                speechToTextData.setDateVisibilityState(View.VISIBLE);
            }
        }
        return speechToTextData;
    }

    @Override
    public void showNoDataFoundText() {
        noDataFoundTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoDataFoundText() {
        noDataFoundTextView.setVisibility(View.GONE);
    }

    @Override
    public void showAudioRecorderInitializationFailedError() {
        Toast.makeText(getBaseContext(), "Audio recorder initialization failed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPreparingApkFileToast() {
        Toast.makeText(getBaseContext(), "Creating apk... wait!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void shareApkFileReady(File file) {
        Uri uri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share this app via"));
    }

    @Override
    public void clearSpeechToTextDataList() {
        speechToTextDataList.clear();
        speechToTextHistoryRecyclerViewAdapter.addAll(speechToTextDataList);
        if (speechToTextDataList.size() == 0) {
            showNoDataFoundText();
        }
    }

//    private void enableOrDisableDeleteAndShareFloatingActionButtons(boolean state) {
//        //recordFloatingActionButton.setEnabled(state);
//        deleteFloatingActionButton.setEnabled(state);
//        //shareFloatingActionButton.setEnabled(state);
//    }


    @Override
    protected void onStart() {
        super.onStart();

        speechToTextPresenter.registerListener(this);


    }

    @Override
    protected void onStop() {
        super.onStop();
        speechToTextPresenter.unregisterListener();
        if (pulsatorLayout.isStarted()) {
            pulsatorLayout.stop();
        }
    }

    public static void setMessage(String oldtext, String newtext) {
        TextView realtime = speechToTextMainActivity.findViewById(R.id.realTimeText);
        speechToTextMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                realtime.setText(oldtext);
            }


        });


    }

    public static String getMessage() {

        return removeDuplicates(textView.getText().toString());
    }

    public static String removeDuplicates(String string) {
        int temp = ((string + string).indexOf(string, 1));
        if (temp != -1) {
            return string.substring(0, temp);
        }
        return string;
    }

    //creation of dataset by the user
    public static void present(double[] audio) {
        File myDirectory = new File(Environment.getExternalStorageDirectory(), "/Music/dataset");

        if (!myDirectory.exists()) {
            System.out.println(myDirectory.getAbsolutePath() + "HERE!");
            myDirectory.mkdirs();
        }

        speechToTextMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new AlertDialog.Builder(speechToTextMainActivity)
                        .setTitle("Add Entry")
                        .setMessage("Do you want to add this entry?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                WriteExample writeExample = new WriteExample();
                                SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
                                String format = s.format(new Date());
                                String audio_name = "audio_" + format + ".wav";
                                writeExample.Write(audio_name, audio);
                                int seconds = audio.length / 16000;
                                File file = new File(Environment.DIRECTORY_MUSIC, "dataset/audio_" + format + ".wav");
                                try {
                                    writeFile(audio_name, getGroundTruth(), String.valueOf(seconds));
                                    recorded.add(getGroundTruth());
                                    SpeechToTextMainActivity.speechToTextMainActivity.recordFloatingActionButton.setClickable(false);
                                    lLayout.setBackgroundColor(Color.parseColor("#F6BCD0"));

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_input_add)
                        .show();

            }
        });

        //audio=new ReadExample().readaudio(new File(audDir,"5.wav"));

    }

    public static void toast(String message) {
        speechToTextMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(speechToTextMainActivity, message, Toast.LENGTH_LONG).show();
            }
        });

    }

    public static int getDuration() {
        return time.get(count);
    }

    public static String getGroundTruth() {
        return groundtruth.get(count);
    }

    public static void writeFile(String mValue, String nvalue, String pvalue) throws IOException {


        try {
//            InputStream filename = assetManager
//                    .open("onDeviceTraining_dataset.csv");

            //System.out.println("|| Writing to Asset Folder ||");

            String filename = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/onDeviceTraining_dataset.csv";
            Log.w("filename", String.valueOf(filename));
            FileWriter fw = new FileWriter(String.valueOf(filename), true);
            fw.write(mValue + "," + nvalue + "," + pvalue);
            fw.write("\n");
            fw.close();
        } catch (IOException ioe) {
        }

    }

    public static boolean isInference() {
        return Inference.isChecked();
    }


}
