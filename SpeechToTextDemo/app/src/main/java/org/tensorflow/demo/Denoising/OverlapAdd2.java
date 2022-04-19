package org.tensorflow.demo.Denoising;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class OverlapAdd2 {
    private static double GetX (double angle, double magnitude)
    {
        return Math.cos(angle) * magnitude;
    }

    private  static double GetY (double angle, double magnitude)
    {
        return Math.sin(angle) * magnitude;
    }
    public static double[] OverlapAdd2(double[][] XNEW,double[][] yphase,double windowLen,double ShiftLen) {

        double FreqRes = XNEW.length;
        double FrameNum = XNEW[1].length;
        Complex[][] Spec = new Complex[(int) (windowLen)][(int) FrameNum];
        Complex temp;
        Complex temp1;
        int start1;
        double x,y;
        for (int i=0;i<FrameNum;i++) {
            for (int j = 0; j < (FreqRes); j++) {
                x=GetX((yphase[j][i]),(XNEW[j][i]));
                y=GetY((yphase[j][i]),(XNEW[j][i]));
                temp = new Complex(x,y);
                Spec[j][i]=temp;
            }
        }
        for (int i=0;i<FrameNum;i++) {
            for (int j = 1; j < FreqRes; j++) {
                x=GetX((yphase[j][i]),(XNEW[j][i]));
                y=GetY((yphase[j][i]),(XNEW[j][i]));
                temp1 = new Complex(x,-1*y);
                Spec[(int) (windowLen-j)][i]=temp1;
            }
        }

        double[] sig= new double [(int) ((FrameNum-1)*ShiftLen+windowLen)];
        double[] weight= new double [(int) ((FrameNum-1)*ShiftLen+windowLen)];
        for (int i=0;i<((FrameNum-1)*ShiftLen+windowLen);i++) {
            sig[i]=0;
            weight[i]=0;
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        Complex[] spec= new Complex[(int) (windowLen)];
        Complex[] ifft_result = new Complex[(int) (windowLen)];
        double start=0;
        int b=0;
        double UR,UI;
        for (int i=0;i<FrameNum;i++) {
            start = (i) * ShiftLen;
            b=0;
            for (int j = 0; j < Spec.length; j++) {
                spec[j] = Spec[j][i];
                UR=spec[j].getReal();
                UI=spec[j].getImaginary();
            }
            ifft_result = fft.transform(spec, TransformType.INVERSE); //X is a Complex Array
            for (int a = (int) start; a < (int) (start + windowLen); a++) {
                sig[a]=(sig[a]+ifft_result[b].getReal());
                b=b+1;
                //System.out.print(sig[a] );
            }
            //System.out.print(sig.length);
        }
        return sig;
    }
    }
