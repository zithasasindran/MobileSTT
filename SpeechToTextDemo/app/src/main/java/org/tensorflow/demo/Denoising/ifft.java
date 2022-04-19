package org.tensorflow.demo.Denoising;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class ifft {
    private static double GetX (float angle, float magnitude)
    {
        return Math.cos(angle) * magnitude;
    }

    private  static double GetY (float angle, float magnitude)
    {
        return Math.sin(angle) * magnitude;
    }
    public static float[] ifft(float[] XNEW,float[] yphase,double windowLen,double ShiftLen)
    {
        double FreqRes = XNEW.length;
        Complex[] Spec = new Complex[(int) (windowLen)];
        Complex temp;
        Complex temp1;
        int start1;
        double x,y;
        for (int j = 0; j < (FreqRes); j++) {
            x=GetX((yphase[j]),(XNEW[j]));
            y=GetY((yphase[j]),(XNEW[j]));
            temp = new Complex(x,y);
            Spec[j]=temp;
        }
        for (int j = 1; j < FreqRes; j++) {
            x=GetX((yphase[j]),(XNEW[j]));
            y=GetY((yphase[j]),(XNEW[j]));
            temp1 = new Complex(x,-1*y);
            Spec[(int) (windowLen-j)]=temp1;
        }

        float[] sig= new float[(int) (windowLen)];

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        Complex[] ifft_result;
        ifft_result = fft.transform(Spec, TransformType.INVERSE); //X is a Complex Array
        for (int a = 0; a < (int) (windowLen); a++) {
            sig[a]= (float) ifft_result[a].getReal();
        }
        //System.out.print(sig.length);
        return sig;
    }
}
