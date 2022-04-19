package org.tensorflow.demo.Denoising;

import android.os.Environment;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.special.BesselJ;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.*;
import java.lang.Object;

import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;


public class LSA {

    private static int L,numberOfFrames;
    private static double W, N, SP;
    private static double[] VAD_sig,Yr;
    private static double[][] Y, Seg;
    private static double[][][] Index;
    double[] wnd;
    static double[][] y;

//    public LSA(@NotNull short[] audioData) {
//
//    }

    public static double[] toDouble(byte[] bytes){
        return new double[]{ByteBuffer.wrap(bytes).getDouble()};
    }

    public static double[] hamming(double W){
        int i;
        int k=1;
        double[] wnd = new double[(int) W];
        for( i=0; i<=((W/2)-1); i++)
        {
            wnd[i]=0.54-0.46*Math.cos(2*Math.PI*(i/(W-1)));
        }
        for(i= (int) (W/2); i<W; i++)
        {
            wnd[i]=wnd[(int) ((W/2)-k)];
            k++;
        }
        return wnd;
    }

    public static double any(double[] array, double value){
        int i;
        double result = 0;
        for( i=0; i<array.length; i++)
        {
            if (array[i]==value){
                result=1;
            }
        }
        return result;
    }

    public static double[][][] repmat (double[] array, int rows, int columns, int depth){
        int arrayColumns = array.length;
        int resultColumns = arrayColumns * columns;
        double[][][] result = new double[depth][rows][resultColumns];
        int z = 0;

        for (int d = 0; d < depth; d++){
            for (int r = 0; r < rows; r++){
                for (int c = 0; c < resultColumns; c++){
                    result[d][r][c] = array[z++];
                    if (z >= arrayColumns){
                        z = 0;
                    }
                }
            }
        }
        return result;
    }

    private static double[][] segment(double[] noisy, double W, double SP, double[] wnd){
        double L=noisy.length;
        int i;
        int start=0;
        double [][] Seg = new double [(int) W][(int) N];
        double[] segment = new double[(int) W];
        for (i=0;i<N;i++)
        {
            for (int j = 0; j<(int) W; j++) {
                segment[j]= noisy[start+j];
                Seg[j][i] = segment[j] * wnd[j];
            }
            start= (int) (start+SP);
        }
        return Seg;
    }

    private static double[][] segment_y(double[] noisy, double W, double SP, double[] wnd){
        double L=noisy.length;
        int i;
        int start=0;
        double [][] Seg = new double [(int) W][(int) N];
        double[] segment = new double[(int) W];
        for (i=0;i<N;i++)
        {
            for (int j = 0; j<(int) W; j++) {
                segment[j]= (double) noisy[start+j];
                Seg[j][i] = segment[j] * wnd[j];
            }
            start= (int) (start+SP);
        }
        return Seg;
    }

    public static double logGamma(double x) {
        double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    static double gamma(double x) { return Math.exp(logGamma(x)); }

    public static double[][] transpose(double[][] array){
        int i=array.length; //number of rows
        int j=array[1].length; //number of columns
        double[][] b=new double[j][i];
        double[][] a;
        a=array;
        for(int r = 0;r<j;r++) {
            for (int c = 0; c < i; c++) {
                b[r][c] = a[c][r];
            }
        }
        return b;
    }

    @NotNull
    public short[] invoke(@NotNull short[] audioData) {
        return new short[0];
    }

    public static class Creal_T{
        public double re = 0;
        public double im = 0;
    }

    public static double min(double val1,double val2)
    {
        double min;
        double Inf=Double.POSITIVE_INFINITY;
        min = Math.min(isNaN(val1) ? Inf : val1, isNaN(val2) ? Inf : val2); // returns minimum
        if (min==Inf)
            min=NaN;
        return min;
    }

    public static double max(double val1,double val2)
    {
        double max;
        double Inf=Double.NEGATIVE_INFINITY;
        max = Math.max(isNaN(val1) ? Inf : val1, isNaN(val2) ? Inf : val2); // returns minimum
        if (max==Inf)
            max=NaN;
        return max;
    }

    public static void process (double[] audio1,int samplingFreq) throws IOException {
        ReadExample read = new ReadExample();
        double[] audio = audio1;

        int fs=samplingFreq;
        //int NB=bitsPerSample;

        L = audio.length;
        W = (.032 * fs);
        long nfft = (long) W;
        SP = 0.5;
        SP = (W * SP); //in frames
        N = Math.floor((L - W) / SP + 1);

        //Seg = new double[(int)W][(int)N];
        y = new double[(int) W][(int) N];
        double[] ones = new double[(int) W];
        //double[][] VAD = new double[(int) W][(int) N];

        y = segment_y(audio, W, SP, hamming(W));
        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
        //Complex[] Y;
        double[] y_segment = new double[(int) W];
        double[][] Yabs = new double[(int) ((W/2)+1)][(int) N];
        double[][] Yphase = new double[(int) ((W/2)+1)][(int) N];
        double[][] pSpec = new double[(int) ((W/2)+1)][(int) N];
        Complex[] Y = new Complex[(int) (W)];


        for (int i=0;i< N;i++) {
            for (int j = 0; j< W; j++)
            {
                y_segment[j] = (y[j][i]);
            }
            Y = fastFourierTransformer.transform(y_segment,TransformType.FORWARD);

            for(int a=0; a<=(W/2); a++) {
                Yabs[a][i] = Math.pow((Math.pow(Y[a].getReal(),2)+Math.pow(Y[a].getImaginary(),2)),0.5);
                Yphase[a][i] = Math.atan2(Y[a].getImaginary(),Y[a].getReal());
                pSpec[a][i]=Math.pow(Yabs[a][i], 2);
            }
        }

        numberOfFrames=Yabs.length;
        double[][] pSpec_t = new double[(int) N][(int) ((W/2)+1)];
        pSpec_t=transpose(pSpec);
        double[][] Dk_t = new double[(int) N][(int) ((W/2)+1)];
        double tinc=0.016;
        MS ms = new MS();
        Dk_t=ms.MS(pSpec_t,tinc);
        double[][] Dk = new double[(int) ((W/2)+1)][(int) N];
        Dk=transpose(Dk_t);

        double order1=1;
        double order0=0;
        double alphaD=0.9;
        double alpha = 0.98; //alpha is the smoothing factor to get apriori_SNR
        double[] prev_gained_SNR=new double[(int) ((W/2)+1)]; //set prev_gained_SNR to 1
        double[] operator=new double[(int) ((W/2)+1)];
        double[] apriori_SNR=new double[(int) ((W/2)+1)];
        double[] apriori_gain_factor=new double[(int) ((W/2)+1)];
        double[] gain=new double[(int) ((W/2)+1)];

        double[][] pos_SNR=new double[(int) ((W/2)+1)][(int)N];
        double[][] magnitude_estimated=new double[(int) ((W/2)+1)][(int)N];
        double[][] totalgain=new double[(int) ((W/2)+1)][(int)N];
        int flag3=0;

        Eint expint = new Eint();
        double[] value =new double[(int) ((W/2)+1)];
        Eint.Creal_T expint_check;

        //set the first frame of snr as array of ones
        for (int j = 0; j < ((W / 2)); j++) {
            prev_gained_SNR[j]=1;
            pos_SNR[j][0]=0.01;
        }
        int flag=0;

        for (int i=0;i<N;i++) {
            for (int j = 0; j <=((W / 2)); j++) {

                //posterior SNR = power / noise
                //the noise here is the actual amplitude of noise signal in time domain
                //clip posterior snr between -20 to + 20d b
                pos_SNR[j][i] = min(max((pSpec[j][i]/Dk[j][i]),0.01),100);
                operator[j] = pos_SNR[j][i] - 1;
                if (operator[j]<0) {
                    operator[j] = 0; //half wave rectify the operator
                }

                //get apriori snr using equation[ 51].clip the negative values
                apriori_SNR[j] = max((alpha * prev_gained_SNR[j] + (1 - alpha) * operator[j]), 0);
                //calculate v using eq[ 8]
                apriori_gain_factor[j] = (apriori_SNR[j] / (1 + apriori_SNR[j]));

                //calculate gain
                value[j] = (apriori_gain_factor[j]*pos_SNR[j][i]);
                //System.out.print("value results " + value[j]);
                expint_check= expint.Eint(value[j]);
                //System.out.print("expint results " + expint_check.re);
                gain[j] = (apriori_gain_factor[j])*Math.exp(0.5*expint_check.re);

                //save the gain of the particular frame
                totalgain[j][i]=min(gain[j],10);
                //calculate prev_gained_SNR to calculate apriori_SNR of the next frame
                prev_gained_SNR[j] = pos_SNR[j][i]* (Math.pow(gain[j],2));

                magnitude_estimated[j][i] = totalgain[j][i]*Math.abs(Yabs[j][i]); //multiply gain with fft magnitude to get mmse amplitude estimate
            }
        }

//        try {
//            FileWriter writer = new FileWriter("C:\\Users\\Zenlab\\Desktop\\S2T\\Dk_java_new.txt");
//            for (int i = 0; i <N; i++)
//            {
//                for (int j = 0; j <= (W/2); j++)
//                {
//                    writer.write(Dk[j][i] + "\n"+ " ");
//                }
//                //writer.write(",");
//            }
//            writer.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }

        OverlapAdd2 OA2 = new OverlapAdd2();
        double[] output;
        output=OverlapAdd2.OverlapAdd2(magnitude_estimated,Yphase,W,SP); //reconstruct using overlap and add method

        WriteExample write = new WriteExample();
        write.Write("DenoisedSample_2.wav",output);

    }

}
