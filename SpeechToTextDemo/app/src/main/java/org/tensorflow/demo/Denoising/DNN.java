package org.tensorflow.demo.Denoising;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;


public class DNN {
    private static int L,numberOfFrames;
    private static double W, N, SP;
    private static double[] VAD_sig,Yr;
    private static double[][] Y, Seg;
    private static double[][][] Index;
    double[] wnd;
    static double[][] y;

    public static double[] toDouble(byte[] bytes){
        return new double[]{ByteBuffer.wrap(bytes).getDouble()};
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

    public static double[][] segment_y(double[] noisy, double W, double SP){
        double L=noisy.length;
        int i;
        int start= 0;
        double [][] Seg = new double [(int) W][(int) N];
        double [] segment = new double [(int) W];
        for (i=0;i<N;i++)
        {
            int m=0;
            //Shift the given array by 128 times toward left
            if (i>0)
            {
                for (int j = (int) SP; j < W; j++) {
                    //Shift element of array by 128
                    segment[m] = Seg[j][i-1];
                    m = m + 1;
                }
            }
            int n=0;
            for (int j = (int) (W-SP); j<(int) W; j++) {
                segment[j] = (double) noisy[start+n];
                n=n+1;
            }
            start= (int) (start+SP);
            for (int j = 0; j<(int) W; j++) {
                Seg[j][i] = segment[j];
            }
        }
        return Seg;
    }

    public static double[] overlapadd(float[][] est_mag){
        double[] denoised = new double[L];
        float [][] out_buffer = new float [(int) W][(int) N];
        for (int i=0;i<N;i++)
        {
            //Shift the given array by 128 times toward left
            if (i>0)
            {
                for (int j = 0; j < (W-SP); j++) {
                    //Shift element of array by 128
                    out_buffer[j][i] = out_buffer[(int) (j+SP)][i-1];
                }
            }
            for (int j = 0; j<(int) W; j++) {
                out_buffer[j][i] += est_mag[j][i];
            }
            int start=0;
            for (int j = (int) (i*SP); j<(int) ((i*SP)+SP); j++) {
                denoised[j] = (double) out_buffer[start][i];
                start=start+1;
            }
        }
        return denoised;
    }

    public static class Creal_T{
        public double re = 0;
        public double im = 0;
    }


    public static float[][] getabs (double[] audio1,int samplingFreq) throws IOException {
        double[] audio = audio1;

        int fs=samplingFreq;
        //int NB=bitsPerSample;

        L = audio.length;
        W = (.032 * fs); //W=512
        long nfft = (long) W;
        SP = 0.25;
        SP = (W * SP); //in frames
        N = Math.floor((L - W) / SP + 1);

        y = new double[(int) W][(int) N];
        double[] ones = new double[(int) W];

        y = segment_y(audio, W, SP);

        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
        double[] y_segment = new double[(int) W];
        float[][] Yabs = new float[(int) ((W/2)+1)][(int) N];
        float[][] Yphase = new float[(int) ((W/2)+1)][(int) N];
        Complex[] Y = new Complex[(int) (W)];

        for (int i=0;i< N;i++) {
            for (int j = 0; j< W; j++)
            {
                y_segment[j] = (y[j][i]);
            }
            Y = fastFourierTransformer.transform(y_segment, TransformType.FORWARD);
            for(int a=0; a<=(W/2); a++) {
                Yabs[a][i] = (float) Math.pow((Math.pow(Y[a].getReal(),2)+Math.pow(Y[a].getImaginary(),2)),0.5);
                Yphase[a][i] = (float) Math.atan2(Y[a].getImaginary(),Y[a].getReal());
            }
        }
        return Yabs;
    }

    public static float[][] getphase (double[] audio1,int samplingFreq) throws IOException {
        double[] audio = audio1;

        int fs=samplingFreq;
        //int NB=bitsPerSample;

        L = audio.length;
        W = (.032 * fs); //W=512
        long nfft = (long) W;
        SP = 0.25;
        SP = (W * SP); //in frames
        N = Math.floor((L - W) / SP + 1);

        //Seg = new double[(int)W][(int)N];
        y = new double[(int) W][(int) N];
        double[] ones = new double[(int) W];

        y = segment_y(audio, W, SP);
        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
        double[] y_segment = new double[(int) W];
        float[][] Yabs = new float[(int) ((W/2)+1)][(int) N];
        float[][] Yphase = new float[(int) ((W/2)+1)][(int) N];
        Complex[] Y = new Complex[(int) (W)];

        for (int i=0;i< N;i++) {
            for (int j = 0; j< W; j++)
            {
                y_segment[j] = (y[j][i]);
            }
            Y = fastFourierTransformer.transform(y_segment, TransformType.FORWARD);
            for(int a=0; a<=(W/2); a++) {
                Yabs[a][i] = (float) Math.pow((Math.pow(Y[a].getReal(),2)+Math.pow(Y[a].getImaginary(),2)),0.5);
                Yphase[a][i] = (float) Math.atan2(Y[a].getImaginary(),Y[a].getReal());
            }
        }
        return Yphase;
    }

    public static float[][][] multiply3Dmatrices(float[][][] firstmatrix, float[][][] secondmatrix){
        float a[][][]=firstmatrix;
        float b[][][]=secondmatrix;
        float c[][][]=new float[1][1][257];

        for(int i=0;i<1;i++){
            for(int j=0;j<1;j++){
                for(int k=0;k<257;k++)
                {
                    c[i][j][k]+=a[i][j][k]*b[i][j][k];
                }
            }
        }
        return c;
    }

    // Function to multiply
    // two matrices A[][] and B[][]
    public static float[] multiplyMatrix(float A[],float B[][][])
    {
        int row2 = B[0].length;
        int col2 = B[0][0].length;

        // Matrix to store the result
        // The product matrix will
        // be of size row1 x col2
        float C[] = new float[col2];

        // Multiply the two marices
        for (int i = 0; i < col2; i++) {
                    C[i] = A[i] * B[0][0][i];
        }
        return C;
    }

}

