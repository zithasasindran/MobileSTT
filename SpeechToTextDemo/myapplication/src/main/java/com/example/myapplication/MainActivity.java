package com.example.myapplication;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity {

    private static int L,numberOfFrames;
    private static double W, N, SP;
    private static double[] VAD_sig;
    private static double[][] Y, Seg, YPhase;
    private static double[][][] Index;
    double[] wnd;
    static double[][] y;

    public static double[] toDouble(byte[] bytes){
        return new double[]{ByteBuffer.wrap(bytes).getDouble()};
    }

    public static double[] hamming(double W){
        int i;
        double[] wnd = new double[(int) W];
        for( i=0; i<W-1; i++)
        {
            wnd[i+1]=0.54-0.46*Math.cos(2*Math.PI*(i/W-1));
        }
        return wnd;
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
        L=noisy.length;
        SP=(W*SP); //0.4*10 = 4 //10
        N=(long) ((L-W)/SP +1);
        int i;
        int start=0;
        double[] segment = new double[(int) N];
        for (i=0;i<N;i++)
        {
            for (int j = 0; j<W; j++) {
                segment[j]=noisy[start+j];
                Seg[j][i] = segment[j] * wnd[j];
            }
            start= (int) (start+SP-1);
        }
        /*Index=(repmat(noisy, W, N,1)+repmat(noisy,(0:(N-1))*SP,1,W));
        int[][][] hw = repmat(wnd, 1, N);
        Seg=noisy[Index]*hw;*/
        return Seg;
    }

    public static double normalize(double rawMax, double rawMin, double value) {
        double abs = Math.abs(value);
        double a = (abs - rawMin) * (8000 - 1000);
        double b = (rawMax - rawMin);
        double result = 1000 + ( a/b );

        // Copy the sign of value to result.
        result = Math.copySign(result,value);
        return result;
    }

    public static void main (String[] args) {

        FFT fft = new FFT();

        VAD20 vad = new VAD20();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in= null;

        {
            try {
                in = new BufferedInputStream(new FileInputStream("/home/zenlabpc/Downloads/input_plane.wav"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        int read=0;
        byte[] buff = new byte[1024];
        while (true) {
            try {
                if (!((read = in.read(buff)) > 0)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.write(buff, 0, read);
        }
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] audioBytes = out.toByteArray();
        int times = Double.SIZE / Byte.SIZE;
        double[] audio = new double[audioBytes.length/times];


        for (int i = 0; i < audio.length; i++) {
            audio[i] = ByteBuffer.wrap(audioBytes, i*times, times).getDouble();
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < audio.length; i++) {
            min = Math.min(min, audio[i]);
            max = Math.max(max, audio[i]);
        }

        for (int i = 0; i < audio.length; i++) {
            audio[i] = normalize(max,min,audio[i]);
            //System.out.println(audio[i]);
        }

        for (int i = 0; i < audio.length; i++) {
            System.out.println(audio[i]);
        }

        VAD_sig = new double[1000];
        Seg = new double[1000][1000];
        y = new double[1000][1000];
        YPhase = new double[1000][1000];
        Y = new double[1000][1000];

        VAD_sig = vad.VAD20(audio);

        L=VAD_sig.length;
        W=(.025*16000);
        long nfft = (long) W;
        SP= 0.4;
        SP= (W*SP);
        N=((L-W)/SP +1);
        //Index=(repmat(audio,1:W,N,1)+repmat(audio,(0:(N-1))*SP,1,W));

        y=segment(audio,W,SP,hamming(W));
        double temp;
        double[] y_segment=new double[audio.length];
        double[][] magSpec=new double[audio.length][audio.length];
        for (int i=0;i< N;i++) {
            for (int j = 0; j< W; j++)
            {
                temp = (y[j][i]);
                y_segment[j] = temp;
            }
            fft.process(y_segment);
            for (int j = 0; j< W; j++) {
                magSpec[j][i] = fft.real[j] * fft.real[j] + fft.imag[j] * fft.imag[j];
            }
        }
        for (int i = 0; i < magSpec.length; i++) {
            for (int j = 0; j < magSpec[i].length; j++) {
                System.out.print(magSpec[i][j] + " ");
            }
            System.out.println();
        }
        /*for(int i = 0; i<=y.length; i++){
            YPhase[i]=Math.atan2(fft.real[i], fft.imag[i]);
        }*/
        /*Y=Math.abs(Y);
        pSpec=Math.pow(Y, 2);
        numberOfFrames=Y.length[1];*/
    }
}