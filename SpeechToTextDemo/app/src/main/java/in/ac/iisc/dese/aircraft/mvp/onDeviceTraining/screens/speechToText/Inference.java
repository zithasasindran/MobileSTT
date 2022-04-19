package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText;

import android.os.Environment;

import org.tensorflow.demo.ReadExample;
import org.tensorflow.demo.mfcc.MelSpectrogram;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Inference {
    public static char[] characters = {' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static String path = "/data/data/in.ac.iisc.dese.aircraft.mvp.speechtotext/files";

    public static String infer(double[] doublearray){
        String result = "";
        try (Interpreter interpreter = new Interpreter(Train.loadModelFile(SpeechToTextMainActivity.speechToTextMainActivity))) {
            // Load the trained weights from the checkpoint file.

            //File outputFile = new File(getFilesDir(), "ds2_960.ckpt");

            File outputFile = new File(path, "final_checkpoint.ckpt");

            System.out.println("filedir" + outputFile.getAbsolutePath());
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("checkpoint_path", outputFile.getAbsolutePath());
            Map<String, Object> outputs = new HashMap<>();
            interpreter.runSignature(inputs, outputs, "restore");
            System.out.println("DS2 WEIGHTS RESTORED");
            //Toast.makeText(this, "Restored Weights", Toast.LENGTH_LONG).show();



            float[][][] y_pred_output = new float[1][291][28];

            // Run the inference.
            ReadExample readExample = new ReadExample();
            File file=new File(Environment.getExternalStorageDirectory(),"Music/dataset");
            //double[] audioo = readExample.readaudio(new File(file ,nextLine[0])); //the location of the wav file
            double[] audioo = doublearray;
            MelSpectrogram melSpectrogram = new MelSpectrogram();
            float[][] melInput = melSpectrogram.process(audioo, 16000);
            float[][][][] tempmel = new float[1][600][80][1];
            System.out.println("MEL FINISHED");

            //filling with zeros
            for(int x=0;x<600;x++){
                for(int y=0;y<80;y++){
                    tempmel[0][x][y][0]= (float) -13.815511;
                }
            }
            //copy the values
            for(int x = 0;x<melInput.length;x++){
                for(int y =0;y<melInput[0].length;y++){
                    if(x<600) {
                        tempmel[0][x][y][0] = melInput[x][y];
                    }
                }
            }


            for(int sample =0 ; sample<1;sample++) {
                Map<String, Object> infer_inputs = new HashMap<>();
                Map<String, Object> infer_outputs = new HashMap<>();

                infer_inputs = new HashMap<>();
                infer_inputs.put("x", tempmel);
                infer_outputs = new HashMap<>();
                infer_outputs.put("output", y_pred_output);


                interpreter.runSignature(infer_inputs, infer_outputs, "infer");

                System.out.println("check" + y_pred_output[0][20][1]);

                int prev = -1;
                // Process the result to get the final category values.
                result = "";
                float maxim = -1000000000;
                int index = -1;
                for (int x = 0; x < 491; ++x) {
                    maxim = -1000000000;
                    index = -1;
                    for (int y = 0; y < 28; ++y) {
                        if (y_pred_output[0][x][y] > maxim) {
                            maxim = y_pred_output[0][x][y];
                            index = y;
                        }
                    }
                    if (index != 27 && index!=prev) {
                        result += characters[index];
                    }
                    prev=index;

                }


                //infer.setText(result);
                System.out.println("this is ur output: -> " + result);
                return(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return(result);
    }
}
