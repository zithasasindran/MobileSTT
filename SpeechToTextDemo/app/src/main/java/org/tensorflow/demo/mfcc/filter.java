package org.tensorflow.demo.mfcc;
import java.util.Arrays;

public class filter {

    public static double[] filter(double[] b, double[] a, double[] x) {
        double[] filter = null;
        double[] a1 = getRealArrayScalarDiv(a,a[0]);
        double[] b1 = getRealArrayScalarDiv(b,a[0]);
        int sx = x.length;
        filter = new double[sx];
        filter[0] = b1[0]*x[0];
        for (int i = 1; i < sx; i++) {
            filter[i] = 0.0;
            for (int j = 0; j <= i; j++) {
                int k = i-j;
                if (j > 0) {
                    if ((k < b1.length) && (j < x.length)) {
                        filter[i] += b1[k]*x[j];
                    }
                    if ((k < filter.length) && (j < a1.length)) {
                        filter[i] -= a1[j]*filter[k];
                    }
                } else {
                    if ((k < b1.length) && (j < x.length)) {
                        filter[i] += (b1[k]*x[j]);
                    }
                }
            }
        }
        return filter;
    }

    public static double[] conv(double[] a, double[] b) {
        double[] c = null;
        int na = a.length;
        int nb = b.length;
        if (na > nb) {
            if (nb > 1) {
                c = new double[na+nb-1];
                for (int i = 0; i < c.length; i++) {
                    if (i < a.length) {
                        c[i] = a[i];
                    } else {
                        c[i] = 0.0;
                    }
                }
                a = c;
            }
            c = filter(b, new double [] {1.0} , a);
        } else {
            if (na > 1) {
                c = new double[na+nb-1];
                for (int i = 0; i < c.length; i++) {
                    if (i < b.length) {
                        c[i] = b[i];
                    } else {
                        c[i] = 0.0;
                    }
                }
                b = c;
            }
            c = filter(a, new double [] {1.0}, b);
        }
        return c;
    }

    public static double[] deconv(double[] b, double[] a) {
        double[] q = null;
        int sb = b.length;
        int sa = a.length;
        if (sa > sb) {
            return q;
        }
        double[] zeros = new double[sb - sa +1];
        for (int i =1; i < zeros.length; i++){
            zeros[i] = 0.0;
        }
        zeros[0] = 1.0;
        q = filter(b,a,zeros);
        return q;
    }

    public static double[] deconvRes(double[] b, double[] a) {
        double[] r = null;
        r = getRealArraySub(b,conv(a,deconv(b,a)));
        return r;
    }

    public static double[] getRealArraySub(double[] dSub0, double[] dSub1) {
        double[] dSub = null;
        if ((dSub0 == null) || (dSub1 == null)) {
            throw new IllegalArgumentException("The array must be defined or diferent to null");
        }
        if (dSub0.length != dSub1.length) {
            throw new IllegalArgumentException("Arrays must be the same size");
        }
        dSub = new double[dSub1.length];
        for (int i = 0; i < dSub.length; i++) {
            dSub[i] = dSub0[i] - dSub1[i];
        }
        return dSub;
    }
    public static double[] getRealArrayScalarDiv(double[] dDividend, double dDivisor) {
        if (dDividend == null) {
            throw new IllegalArgumentException("The array must be defined or diferent to null");
        }
        if (dDividend.length == 0) {
            throw new IllegalArgumentException("The size array must be greater than Zero");
        }
        double[] dQuotient = new double[dDividend.length];

        for (int i = 0; i < dDividend.length; i++) {
            if (!(dDivisor == 0.0)) {
                dQuotient[i] = dDividend[i]/dDivisor;
            } else {
                if (dDividend[i] > 0.0) {
                    dQuotient[i] = Double.POSITIVE_INFINITY;
                }
                if (dDividend[i] == 0.0) {
                    dQuotient[i] = Double.NaN;
                }
                if (dDividend[i] < 0.0) {
                    dQuotient[i] = Double.NEGATIVE_INFINITY;
                }
            }
        }
        return dQuotient;
    }

    public static void process() {
        double[] a, b, q, u, v, w, r, z, input, outputVector;


        u = new double [] {1,1,1};
        v = new double [] {1, 1, 0, 0, 0, 1, 1};
        w = conv(u,v);
        System.out.println("w=\n"+ Arrays.toString(w));

        a = new double [] {1, 2, 3, 4};
        b = new double [] {10, 40, 100, 160, 170, 120};
        q = deconv(b,a);
        System.out.println("q=\n"+Arrays.toString(q));

        r = deconvRes(b,a);
        System.out.println("r=\n"+Arrays.toString(r));

        a = new double [] {2, -2.5, 1};
        b = new double [] {0.1, 0.1};
        u = new double[31];
        for (int i = 1; i < u.length; i++) {
            u[i] = 0.0;
        }
        u[0] = 1.0;
        z = filter(b, a, u);
        System.out.println("z=\n"+Arrays.toString(z));

        a = new double [] {1.0000};
        b = new double [] {1, -0.97};
        input = new double[]{1,2,3,4,5,6,7,8,9};

        outputVector = filter(b, a, input);
        System.out.println("outputVector=\n"+Arrays.toString(outputVector));
    }
}
