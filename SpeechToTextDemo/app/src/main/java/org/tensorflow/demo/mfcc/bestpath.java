package org.tensorflow.demo.mfcc;
import java.io.IOException;
import java.util.ArrayList;

public class bestpath {

    public static double[] argmax(float[][] matrix) {
        double[] bestpath_array = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            float minInRow = matrix[i][0];
            float maxInRow = matrix[i][0];
            for (int j = 0; j < matrix[i].length; j++) {
                if (minInRow > matrix[i][j]) {
                    minInRow = matrix[i][j];
                }

                if (maxInRow < matrix[i][j]) {
                    maxInRow = matrix[i][j];
                    bestpath_array[i]=j;
                }
            }
            maxInRow = matrix[i][0];
        }
        //System.out.println(Arrays.toString(bestpath_array));
        return bestpath_array;
    }

    public static ArrayList removeblanks(double[] array, double idx){
        ArrayList outarray = new ArrayList();
        int j=0;
        for(int i=0;i<array.length;i++){
            if(array[i]!=idx){
                outarray.add(array[i]);
                j++;
            }
        }
        return outarray;
    }
    public static ArrayList groupby(ArrayList array) {
        ArrayList outarray = new ArrayList();
        array.add(array.size(),100.0); //add a final element at the end that acts as stop byte. Make sure this number is between (0,29) or the number of characters you are passing
        int i=0;
        int j = 0;
        while (j < array.size()-1){
            i=j;
            outarray.add(array.get(j));
            while ((double) array.get(j) == (double) array.get(++i)) {
            }
            j=i;
        }
        return outarray;
    }

    public static String process (float[][][] probmatrix_3D, int mfccinputlength) throws IOException {
        float[][] probmatrix = new float[probmatrix_3D[0].length][probmatrix_3D[0][0].length];
        //convert to 2d float
        for(int i=0;i<probmatrix_3D.length;i++){
            for(int j=0;j<mfccinputlength;j++){
                for(int k=0;k<probmatrix_3D[0][0].length;k++){
                    probmatrix[j][k]=probmatrix_3D[i][j][k];
                }

            }

        }
            double[] bestpath = argmax(probmatrix);
            double blank_idx = 28;
            ArrayList bestpath_removed_blanks = removeblanks(bestpath,blank_idx);
            ArrayList groupby_result = groupby(bestpath_removed_blanks);
            String final_sentence = "";
            String[] classes = {" ","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","-"};
            for (int i=0;i<groupby_result.size();i++){
                double val = (double) groupby_result.get(i);
                final_sentence += (String) classes[(int) val];
            }
            //System.out.println(final_sentence);

            return final_sentence;
        }
    }
