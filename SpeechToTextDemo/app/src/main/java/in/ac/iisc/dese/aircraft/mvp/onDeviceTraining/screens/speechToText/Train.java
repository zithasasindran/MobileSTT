package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.units.qual.A;
import org.tensorflow.demo.ReadExample;
import org.tensorflow.demo.featureextraction.calltokenizer;
import org.tensorflow.demo.mfcc.MelSpectrogram;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.tensorflow.demo.featureextraction.calltokenizer;

public class Train {

    static ArrayList<Float> cerlist = new ArrayList<Float>();
    static ArrayList<Float> werlist = new ArrayList<Float>();
    static ArrayList<Float> bertlist = new ArrayList<Float>();
    static ArrayList<int[][]> batcheslabels;
    static ArrayList<String> stringLables;
    static ArrayList<float[][][][]> batches;
    static ArrayList<float[][][][]> batcheswer;

    private static int noOfvalidationsamples = 20;
    private static int ypredframesno = 291; //441 for 900 frames //291 for 600 frames
    private static int inputframeno = 600;
    private static int noOfTrainingSamples = 60;
    private static int noOfMelFeatures = 80;




    public static MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("MYMODEL.tflite");
        Log.v("TRAIN","model is loaded");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static MappedByteBuffer loadBERTModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("mobilebert_working.tflite");
        Log.v("BERT","model is loaded");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Load dictionary from assets. */
    public static Map<String, Integer> loadDictionaryFile(Activity activity) throws IOException {
        Map<String, Integer> dic = new HashMap<>();
        try (InputStream ins = activity.getAssets().open("vocab.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            int index = 0;
            while (reader.ready()) {
                String key = reader.readLine();
                dic.put(key, index++);
            }
        }
        Log.v("Loaded dictionary is : ", String.valueOf(dic));
        return dic;
    }

    public static String path = "/data/data/in.ac.iisc.dese.aircraft.mvp.speechtotext/files";
    public static String Name = "newmodel.ckpt";

    public static void copyweights(String filename) throws IOException {

        AssetManager assetManager = SpeechToTextMainActivity.speechToTextMainActivity.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(filename);
            String newFileName = path + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[120983552];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("COPY WEIGHTS FAILED: ", e.getMessage());
        }
    }

    public static void copygroundtruth() {
        AssetManager assetManager = SpeechToTextMainActivity.speechToTextMainActivity.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open("ground_truth.csv");
            String newFileName = Environment.getExternalStorageDirectory() + "/" + "ground_truth.csv";
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[7848];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("COPY WEIGHTS FAILED: ", e.getMessage());
        }
    }

    public static void copyondevicetraincsv() {
        AssetManager assetManager = SpeechToTextMainActivity.speechToTextMainActivity.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open("onDeviceTraining_dataset.csv");
            String newFileName = Environment.getExternalStorageDirectory() + "/" + "onDeviceTraining_dataset.csv";
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[0];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("COPY FILES FAILED: ", e.getMessage());
        }


    }

    public static Boolean isCSVFull() throws IOException {
        String filename = "/onDeviceTraining_dataset.csv";
        int count = 0;
        try {
            File filleddataset = new File(Environment.getExternalStorageDirectory() + filename);
            CSVReader reader = new CSVReader(new FileReader(filleddataset));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                System.out.println(nextLine[0] + nextLine[1] + "etc...");
                count += 1;
            }
        } catch (IOException | CsvValidationException e) {
            System.out.println("IO ISSUE!!!!!");
        }
        System.out.println("number of lines" + count);
        if (count >= 70) {
            return (true);
        } else {
            return (false);
        }
    }

    public static void generateNote(Context context, String sBody) {
        try {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "TrainingLog");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "TrainingLog.txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.write(sBody);

            writer.flush();
            writer.close();
            // Toast.makeText(context, "Writting to TXT File Named TrainingLog", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateInference(ArrayList<float[][][][]> matrixforinferlist, ArrayList<String> labels, Activity mcontext) throws IOException {

        char[] characters = {' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        float totalcer = 0f;
        float totalwer = 0f;
        float totalbert = 0f;

        Interpreter bert_interpreter = new Interpreter(Train.loadBERTModelFile(SpeechToTextMainActivity.speechToTextMainActivity));
        Map<String, Integer> dic = loadDictionaryFile(SpeechToTextMainActivity.speechToTextMainActivity);
        calltokenizer ct = new calltokenizer();

        for (int sample = 0; sample < matrixforinferlist.size(); sample++) {
            try (Interpreter another_interpreter = new Interpreter(loadModelFile(mcontext))) {
                // Load the trained weights from the checkpoint file.

                File outputFile = new File(SpeechToTextMainActivity.speechToTextMainActivity.getFilesDir(), "newmodel.ckpt");

                //File outputFile = new File(getFilesDir(), "newmodel.ckpt");

                System.out.println("filedir" + outputFile.getAbsolutePath());
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("checkpoint_path", outputFile.getAbsolutePath());
                Map<String, Object> outputs = new HashMap<>();
                another_interpreter.runSignature(inputs, outputs, "restore");
                System.out.println("DS2 WEIGHTS RESTORED");
                //Toast.makeText(this, "Restored Weights", Toast.LENGTH_LONG).show();


                float[][][] y_pred_output = new float[1][ypredframesno][28];

                // Run the inference.


                Map<String, Object> infer_inputs = new HashMap<>();
                Map<String, Object> infer_outputs = new HashMap<>();

                infer_inputs = new HashMap<>();
                infer_inputs.put("x", matrixforinferlist.get(sample));
                infer_outputs = new HashMap<>();
                infer_outputs.put("output", y_pred_output);


                another_interpreter.runSignature(infer_inputs, infer_outputs, "infer");

                //System.out.println("check" + y_pred_output[0][20][1]);

                int prev = -1;


                // Process the result to get the final category values.
                String result = "";
                float maxim = -1000000000;
                int index = -1;

                for (int x = 0; x < ypredframesno; ++x) {
                    maxim = -1000000000;
                    index = -1;
                    for (int y = 0; y < 28; ++y) {
                        if (y_pred_output[0][x][y] > maxim) {
                            maxim = y_pred_output[0][x][y];
                            index = y;
                        }
                    }
                    if (index != 27 && index != prev) {
                        result += characters[index];
                    }
                    prev = index;

                }

                float mobilebertval = ct.predict(bert_interpreter,dic, labels.get(60 + sample), result);


                System.out.println("this is ur output:  -> " + result);
                generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), result+',');
                float wer = LER_RemovedSpaces.wer(labels.get(60 + sample), result, Boolean.FALSE, " ");
                generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), String.valueOf(wer)+',');
                float cer = LER_RemovedSpaces.cer(labels.get(60 + sample), result, Boolean.FALSE, Boolean.FALSE);
                generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), String.valueOf(cer)+',');
                generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), String.valueOf(mobilebertval)+'\n');

                totalcer += cer;
                totalwer += wer;
                totalbert += mobilebertval;
                System.out.println("\nWER  = " + wer);
                System.out.println("\nLER  = " + cer);


                result = "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cerlist.add(totalcer / noOfvalidationsamples);
        werlist.add(totalwer / noOfvalidationsamples);
        bertlist.add(totalbert / noOfvalidationsamples);

        String tempdisplay = "AVG WER" + Arrays.toString(werlist.toArray()) + "\n" + "AVG CER" + Arrays.toString(cerlist.toArray()) + "\n" + "AVG BERT" + Arrays.toString(bertlist.toArray());
        SpeechToTextMainActivity.setMessage(tempdisplay, "");
        generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), tempdisplay);
        System.out.println(tempdisplay);

        bert_interpreter.close();
        dic.clear();


    }

    static void trainthemodel() throws IOException {

        File ckptfile = new File(path + "/" + "newmodel.ckpt");
        File finalckpt = new File(path + "/" + "final_checkpoint.ckpt");

        if (!ckptfile.exists()) {
            copyweights("newmodel.ckpt");
        }
        if (!finalckpt.exists()) {
            copyweights("final_checkpoint.ckpt");
        }


        char[] characters = {' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        try {
            SpeechToTextMainActivity.setMessage("Training Started Please Hold", "");
            batches = new ArrayList<float[][][][]>();
            batcheslabels = new ArrayList<>();
            stringLables = new ArrayList<>();
            batcheswer = new ArrayList<>();
            werlist = new ArrayList<>();
            int batchnumber = 0;
            float[][][][] tempmel = new float[5][inputframeno][noOfMelFeatures][1];
            int[][] tempytrue = new int[5][150];


            CSVReader reader = new CSVReader(new FileReader(new File(Environment.getExternalStorageDirectory(), "onDeviceTraining_dataset.csv")));
            String[] nextLine;
            int linenum = 1;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                stringLables.add(nextLine[1]);
                System.out.println(nextLine[0] + nextLine[1] + "etc...");
                ReadExample readExample = new ReadExample();
                System.out.println(Environment.DIRECTORY_MUSIC);
                File file = new File(Environment.getExternalStorageDirectory(), "Music/dataset");
                double[] audioo = readExample.readaudio(new File(file, nextLine[0]));
                MelSpectrogram melSpectrogram = new MelSpectrogram();
                float[][] melInput = melSpectrogram.process(audioo, 16000);
                System.out.println("MEL FINISHED");
                SpeechToTextMainActivity.setMessage("Training Started Please Hold" +"\n MEL CONVERSION: "+linenum,"");
                //1-60 only TRAINING SET
                if (linenum <= noOfTrainingSamples) {
                    //filling with zeros
                    for (int x = 0; x < inputframeno; x++) {
                        for (int y = 0; y < noOfMelFeatures; y++) {
                            tempmel[batchnumber][x][y][0] = (float) -13.815511;
                        }
                    }

                    //copy the values
                    for (int x = 0; x < melInput.length; x++) {
                        for (int y = 0; y < melInput[0].length; y++) {
                            //TRUNCATING AT required frame TF
                            if (x < inputframeno) {
                                tempmel[batchnumber][x][y][0] = melInput[x][y];
                            }
                        }
                    }

                    String tempstring = nextLine[1];
                    int tempvar = 0;
                    for (char c : tempstring.toCharArray()) {
                        tempytrue[batchnumber][tempvar] = ArrayUtils.indexOf(characters, c);
                        tempvar++;
                    }
                    while (tempvar < 150) {
                        tempytrue[batchnumber][tempvar] = 27;
                        tempvar++;
                    }
                    batchnumber++;
                    if (batchnumber == 5) {
                        batches.add(tempmel);
                        batcheslabels.add(tempytrue);
                        tempmel = new float[batchnumber][inputframeno][noOfMelFeatures][1];
                        tempytrue = new int[batchnumber][150];
                        batchnumber = 0;
                    }
                }
                // 61-70 only VALIDATION SET
                else {
                    //for validation set!
                    float temp1bs[][][][] = new float[1][inputframeno][noOfMelFeatures][1];
                    //filling with zeros
                    for (int x = 0; x < inputframeno; x++) {
                        for (int y = 0; y < noOfMelFeatures; y++) {
                            temp1bs[0][x][y][0] = (float) -13.815511;
                        }
                    }

                    //copy the values
                    for (int x = 0; x < melInput.length; x++) {
                        for (int y = 0; y < melInput[0].length; y++) {
                            if (x < inputframeno) {
                                temp1bs[0][x][y][0] = melInput[x][y];
                            }
                        }
                    }
                    batcheswer.add(temp1bs);
                }


                //increase line number
                linenum += 1;


            }
        } catch (IOException | CsvValidationException e) {

            System.out.println("!!!CSV File Not Read!!!");

        }

        //Toast.makeText(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), "TRAINING STARTED HOLD ON", Toast.LENGTH_LONG).show();
        try (Interpreter interpreter = new Interpreter(Train.loadModelFile(SpeechToTextMainActivity.speechToTextMainActivity))) {
            //FOR LOADING A CHECKPOINT (DS2 IN THIS CASE)

            File outputFile = new File(path, "newmodel.ckpt");
            System.out.println("filedir" + outputFile.getAbsolutePath());
            Map<String, Object> rinputs = new HashMap<>();
            rinputs.put("checkpoint_path", outputFile.getAbsolutePath());
            Map<String, Object> routputs = new HashMap<>();
            interpreter.runSignature(rinputs, routputs, "restore");
            System.out.println("DS2 WEIGHTS RESTORED");
            generateInference(batcheswer, stringLables, SpeechToTextMainActivity.speechToTextMainActivity);

            //initialize min_wer with baseline wer
            float min_wer = werlist.get(0);
            int epoch = 1;
            int continuetraining =1 ;
            int checkextraepoch = 0 ;

            /*

            !!!NEED TO IMPLEMENT EARLY STOPPING HERE!!!

             */


            //while (epoch < 10) {
            while (continuetraining==1) {


                String logvalue = "\n=================================\nEPOCH NUMBER " + epoch + "\n";
                System.out.println(logvalue);
                //console.setText(logvalue);
                generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), logvalue);

                //generateInference(batcheswer, stringLables, SpeechToTextMainActivity.speechToTextMainActivity);
                //START TRAINING
                long start = System.currentTimeMillis();
                //float avgloss = 0f;
                for (int i = 0; i < batches.size(); ++i) {
                    SpeechToTextMainActivity.setMessage("TRAINING:\n" + "EPOCH: " + String.valueOf(epoch) + "\n Batch: " + String.valueOf(i), "");
                    File reoutputFile = new File(path, "newmodel.ckpt");
                    System.out.println("filedir" + reoutputFile.getAbsolutePath());
                    Map<String, Object> reinputs = new HashMap<>();
                    reinputs.put("checkpoint_path", reoutputFile.getAbsolutePath());
                    Map<String, Object> reoutputs = new HashMap<>();
                    interpreter.runSignature(reinputs, reoutputs, "restore");
                    System.out.println("DS2 WEIGHTS RESTORED");


                    Map<String, Object> inputs = new HashMap<>();
                    inputs.put("x", batches.get(i));
                    inputs.put("y", batcheslabels.get(i));
                    Map<String, Object> outputs = new HashMap<>();
                    FloatBuffer loss = FloatBuffer.allocate(5);
                    outputs.put("loss", loss);
                    System.out.println("BATCH: " + i);
                    interpreter.runSignature(inputs, outputs, "train");
                    System.out.println("ctc loss for batch -> " + loss);

                    //save the trained batch into the checkpoint
                    File saveoutputFile = new File(path, "newmodel.ckpt");
                    Map<String, Object> saveinputs = new HashMap<>();
                    saveinputs.put("checkpoint_path", saveoutputFile.getAbsolutePath());
                    Map<String, Object> saveoutputs = new HashMap<>();
                    interpreter.runSignature(saveinputs, saveoutputs, "save");

                }
                long end = System.currentTimeMillis();
                //finding the time difference and converting it into seconds
                float sec = (end - start) / 1000F;
                System.out.println(sec + " seconds");
                logvalue = "TRAINING FINISHED" + "\n" + sec + " seconds";
                SpeechToTextMainActivity.setMessage(logvalue, "");
                generateNote(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), logvalue);

                //generate inference at end of each epoch
                generateInference(batcheswer, stringLables, SpeechToTextMainActivity.speechToTextMainActivity);

                //stopping criteria
                if (werlist.get(epoch) <= min_wer) {
                    min_wer = werlist.get(epoch);

                    //save checkpoint
                    File saveoutputFile = new File(path, "final_checkpoint.ckpt");
                    Map<String, Object> saveinputs = new HashMap<>();
                    saveinputs.put("checkpoint_path", saveoutputFile.getAbsolutePath());
                    Map<String, Object> saveoutputs = new HashMap<>();
                    interpreter.runSignature(saveinputs, saveoutputs, "save");
                }
                // if checkextraepoch is set to true then stop training
                if (checkextraepoch==1) {
                    continuetraining = 0;
                }

                //if wer increases even once, set checkextraepoch flag as true. The training will continue for one more epoch and stop
                if (epoch>1){
                    if ((werlist.get(epoch) >= werlist.get(epoch-1)) && (checkextraepoch==0)) {
                        checkextraepoch = 1;
                    }
                }

                epoch+=1;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

