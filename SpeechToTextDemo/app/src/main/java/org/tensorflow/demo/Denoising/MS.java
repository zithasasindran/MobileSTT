package org.tensorflow.demo.Denoising;
//import javax.swing.*;

import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
//import static jdk.nashorn.internal.objects.Global.Infinity;
import static org.apache.commons.math3.stat.StatUtils.sum;

public class MS {

    private static double[] mhvals(double d) {
        //Values are taken from Table 5 in [2]
        //[2] R. Martin,"Bias compensation methods for minimum statistics noise power
        //spectral density estimation", Signal Processing Vol 86, pp1215-1229, 2006.
        //approx: plot(d.^(-0.5),[m 1-d.^(-0.5)],'x-'), plot(d.^0.5,h,'x-')
        int i;
        int j;
        double m;
        double h;
        double[][] dmh = {
                {1, 0, 0},
                {2, 0.26, 0.15},
                {5, 0.48, 0.48},
                {8, 0.58, 0.78},
                {10, 0.61, 0.98},
                {15, 0.668, 1.55},
                {20, 0.705, 2},
                {30, 0.762, 2.3},
                {40, 0.8, 2.52},
                {60, 0.841, 3.1},
                {80, 0.865, 3.38},
                {120, 0.89, 4.15},
                {140, 0.9, 4.35},
                {160, 0.91, 4.25},
                {180, 0.92, 3.9},
                {220, 0.93, 4.1},
                {260, 0.935, 4.7},
                {300, 0.94, 5}
        };

        double[] i_array= new double[dmh.length];
        int b=0;
        int flag=0;
        for (int a=0;a<dmh.length;a++) {
            if (d<=dmh[a][0]) {
                i_array[b]=a;
                b++;
                flag=1;
            }
        }

        if (flag==0) {
            i = dmh[0].length;
            j = i;
        }
        else{
            i = (int) i_array[0];
            j = i - 1;
        }
        if (d == dmh[i][0]) {
            m = dmh[i][1];
            h = dmh[i][2];
        }
        else{
            double qj = Math.sqrt(dmh[i - 1][0]);    //interpolate using sqrt(d)
            double qi = Math.sqrt(dmh[i][0]);
            double q = Math.sqrt(d);
            h = dmh[i][2] + (q - qi) * (dmh[j][2] - dmh[i][2]) / (qj - qi);
            m = dmh[i][1] + (qi * qj / q - qj) * (dmh[j][1] - dmh[i][1]) / (qi - qj);
        }
        double[] result = new double[2];
        result[0]=m;
        result[1]=h;
        return result;
    }

    private static double min(double val1,double val2)
    {
        double min;
        double Inf=Double.POSITIVE_INFINITY;
        min = Math.min(isNaN(val1) ? Inf : val1, isNaN(val2) ? Inf : val2); // returns minimum
        if (min==Inf)
            min=NaN;
        return min;
    }

    private static double max(double val1,double val2)
    {
        double max;
        double Inf=Double.NEGATIVE_INFINITY;
        max = Math.max(isNaN(val1) ? Inf : val1, isNaN(val2) ? Inf : val2); // returns minimum
        if (max==Inf)
            max=NaN;
        return max;
    }

    public static double[][] MS(double[][] yf, double tz) {
        double tinc = tz;          //second argument is frame increment
        double nrcum = 0;          //no frames so far
        //algorithm constants
        double Inf = Double.POSITIVE_INFINITY;
        double taca = 0.0449;    //smoothing time constant for alpha_c = -tinc / log(0.7) in equ (11)
        double tamax = 0.392;    //max smoothing time constant in(3) = -tinc / log(0.96)
        double taminh = 0.0133;   //min smoothing time constant (upper limit)in(3) = -tinc / log(0.3)
        double tpfall = 0.064;   //time constant for P to fall(12)
        double tbmax = 0.0717;   //max smoothing time constant in(20) = -tinc / log(0.8)
        double qeqmin = 2;       //minimum value of Qeq (23)
        double qeqmax = 14;      //max value of Qeq per frame
        double av = 2.12;        //fudge factor for bc calculation (23 + 13 lines)
        double td = 1.536;       //time to take minimum over
        double nu = 8;           //number of subwindows
        double[] qith = {0.03, 0.05, 0.06, Inf}; //noise slope thresholds in dB / s
        double[] nsmdb = {47, 31.4, 15.7, 4.1};

        //derived algorithm constants

        double aca = Math.exp(-tinc / taca); //smoothing constant for alpha_c in equ (11) = 0.7
        double acmax = aca;          //min value of alpha_c = 0.7 in equ (11) also = 0.7
        double amax = Math.exp(-tinc / tamax); //max smoothing constant in (3) = 0.96
        double aminh = Math.exp(-tinc / taminh); //min smoothing constant (upper limit) in (3) = 0.3
        double bmax = Math.exp(-tinc / tbmax); //max smoothing constant in (20) = 0.8
        double snrexp = -tinc / tpfall;
        double nv = Math.round(td / (tinc * nu));    //length of each subwindow in frames
        if (nv < 4) {           //algorithm doesn't work for miniscule frames
            nv = 4;
            nu = max(Math.round(td / (tinc * nv)), 1);
        }
        double nd = nu * nv;           //length of total window in frames

        double[] MDVALS=mhvals(nd); //calculate the constants M(D) and H(D) from Table III
        double md=MDVALS[0];
        double hd=MDVALS[1];
        double[] MVVALS=mhvals(nv);
        double mv=MVVALS[0];
        double hv=MVVALS[1];

        double[] nsms = new double[(int) nsmdb.length];
        for (int i = 0; i < nsmdb.length; i++) {
            nsms[i] = Math.pow(10, (nsmdb[i] * nv * tinc / 10));  //[8 4 2 1.2] in paper
        }
        double qeqimax = 1 / qeqmin;  //maximum value of Qeq inverse (23)
        double qeqimin = 1 / qeqmax; //minumum value of Qeq per frame inverse

        double nr = yf.length;          //number of frames and freq bins
        double nrf = yf[0].length;
        double[][] x = new double[(int) nr][(int) nrf];            //initialize output arrays

        //initialize values for first frame
        double[] p = new double[(int) nrf];
        double[] sn2 = new double[(int) nrf];
        double[] pb = new double[(int) nrf];
        double[] pb2 = new double[(int) nrf];
        double[] pminu = new double[(int) nrf];
        double[] actmin = new double[(int) nrf];
        double[] actminsub = new double[(int) nrf];
        double[] lminflag = new double[(int) nrf];
        double ac = 1;               // correction factor (9)
        for (int i = 0; i < nrf; i++) {
            p[i] = yf[0][i];          //smoothed power spectrum
            sn2[i] = p[i];              // estimated noise power
            pb[i] = p[i];               // smoothed noisy speech power (20)
            pb2[i] = Math.pow(pb[i], 2);
            pminu[i] = p[i];
            actmin[i] = Inf;   // Running minimum estimate
            actminsub[i] = actmin[i];           // sub-window minimum estimate
            lminflag[i] = 0;      // flag to remember local minimum
        }
        double subwc = nv;                   // force a buffer switch on first loop
        double[][] actbuf = new double[(int) nu][(int) nrf];  // buffer to store subwindow minima
        for (int j = 0; j < nu; j++) {
            for (int i = 0; i < nrf; i++) {
                actbuf[j][i] = Inf;
            }
        }
        double ibuf = 7;

        //start computations
        double[] yft = new double[(int) nrf];
        double[] ah = new double[(int) nrf];
        double[] b = new double[(int) nrf];
        double[] qeqi = new double[(int) nrf];
        double[] qisq = new double[(int) nrf];
        double[] bmind = new double[(int) nrf];
        double[] bminv = new double[(int) nrf];
        double[] kmod = new double[(int) nrf];
        double[] kmod_delta = new double[(int) nrf];
        double[] lmin = new double[(int) nrf];
        double[] qarray = new double[(int) qith.length];
        double acb;
        double snr;
        double qiav;
        double bc;
        double min_actbuf;
        double nsm;
        boolean boolval;
        double v=0;

        for (int r = 0; r < nr; r++) {             // we use r instead of lambda in the paper
            snr = sum(p) / sum(sn2);
            for (int c = 0; c < nrf; c++) {
                yft[c] = yf[r][c];                   //noise speech power spectrum
                ah[c] = amax * ac * Math.pow((1 + Math.pow((p[c] / sn2[c] - 1), 2)), (-1));    //alpha_hat:smoothing factor per frequency (11)
                ah[c] = max(ah[c], min(aminh, Math.pow(snr, snrexp)));       //lower limit for alpha_hat(12)
            }
            acb = Math.pow((1 + Math.pow((sum(p) / sum(yft) - 1), 2)), -1);  //alpha_c - bar(t) (9)
            ac = aca * ac + (1 - aca) * max(acb, acmax);       //alpha_c(t) (10)

            for (int c = 0; c < nrf; c++) {
                p[c] = ah[c] * p[c] + (1 - ah[c]) * yft[c];            //smoothed noisy speech power (3)
                b[c] = min(Math.pow(ah[c], 2), bmax);              //smoothing constant for estimating periodogram variance(22 + 2 lines)
                pb[c] = b[c] * pb[c] + (1 - b[c]) * p[c];            //smoothed periodogram (20)
                pb2[c] = b[c] * pb2[c] + (1 - b[c])*Math.pow(p[c], 2);        //smoothed periodogram squared(21)
                v = (pb2[c] - Math.pow(pb[c], 2) )/ (2*(Math.pow(sn2[c], 2) ));
                qeqi[c] = max(min( v , qeqimax), (qeqimin / (r + nrcum + 1)));   //Qeq inverse (23)
                bmind[c] = 1 + 2 * (nd - 1) * (1 - md) / (Math.pow(qeqi[c], (-1)) - 2 * md);      //we use the simplified form(17) instead of (15)
                bminv[c] = 1 + 2 * (nv - 1) * (1 - mv) / (Math.pow(qeqi[c], (-1)) - 2 * mv);      //same expression but for sub windows
            }
            qiav = sum(qeqi) / nrf;             //average over all frequencies (23 + 12 lines)(ignore non - duplication of DC and nyquist terms)
            bc = 1 + av * Math.sqrt(qiav);             //bias correction factor(23 + 11 lines)

            for (int c = 0; c < nrf; c++) {
                if ((bc * p[c] * bmind[c]) < actmin[c]) { //frequency mask for new minimum
                    actmin[c] = bc * p[c] * bmind[c];
                    actminsub[c] = bc * p[c] * bminv[c];
                    kmod[c] = 1;
                }
                else {
                    kmod[c]=0;
                }
            }

            if (subwc > 0 && subwc < (nv-1) ) {// middle of buffer -allow a local minimum
                for (int c = 0; c < nrf; c++) {
                    lminflag[c] = (int) lminflag[c] | (int) kmod[c];        //potential local minimum frequency bins
                    pminu[c] = min(actminsub[c], pminu[c]);
                    sn2[c] = pminu[c];
                }
            } else {
                    if (subwc >= (nv-1)){ //end of buffer - do a buffer switch
                        ibuf = ((ibuf+1) % nu);        //increment actbuf storage pointer
                        for (int c = 0; c < nrf; c++) {
                            actbuf[(int) ibuf][c] = actmin[c];            //save sub -window minimum
                        }
                        for (int c = 0; c < nrf; c++) {
                            min_actbuf = actbuf[0][c];
                            for (int a = 0; a < actbuf.length; a++) {
                                if (actbuf[a][c] < min_actbuf) {
                                    min_actbuf = actbuf[a][c];
                                }
                            }
                            pminu[c]=min_actbuf;
                        }
                        //Matlab line : i=find(qiav<qith);
                        int index=0;
                        for (int a=0;a<qith.length;a++){
                            if (qiav<qith[a]){
                                qarray[index]=a;
                                index++;
                            }
                        }
                        //Matlab line : nsm=nsms(i(1));
                        nsm = nsms[(int) qarray[0]];            //noise slope max

                        for (int c = 0; c < nrf; c++) {
                            if (kmod[c] == 1)
                                kmod_delta[c] = 0;
                            else
                                kmod_delta[c] = 1;
                            boolean val1 = actminsub[c] < (nsm * pminu[c]);
                            boolean val2 = actminsub[c] > pminu[c];
                            boolean val3;
                            boolean val4;
                            if (lminflag[c] == 1)
                                val3 = true;
                            else
                                val3 = false;
                            if (kmod_delta[c] == 1)
                                val4 = true;
                            else
                                val4 = false;

                            boolval = (val3 & val4 & val1 & val2);
                            if (boolval == true) {
                                pminu[c] = actminsub[c];
                                for (int a = 0; a < nu; a++) {
                                    actbuf[a][c] = (pminu[c]);
                                }
                            }
                        }
                        for (int c = 0; c < nrf; c++) {
                            lminflag[c] = 0;
                            actmin[c] = Inf;
                        }
                        subwc = -1;
                    }
                }
            for (int c = 0; c < nrf; c++) {
                x[r][c] = sn2[c];
                qisq[c] = Math.sqrt(qeqi[c]);
            }
            subwc = subwc + 1;
        }
        return x;
    }
}
