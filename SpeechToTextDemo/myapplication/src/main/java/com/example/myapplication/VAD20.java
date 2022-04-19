package com.example.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.log;
import static java.lang.Math.max;

public class VAD20 {

    private static double[] Seg;
    private static double[] Ym;

    private static double[] segment(double[] noisy, double W){
        int L=noisy.length;
        long N=(long) Math.round(L/W +1);
        int i;
        int start=0;
        double temp=0;
        double[] segment = new double[(int) N];
        for (i=0;i<N;i++)
        {
            for (int j = 0; j<W; j++) {
                segment[j]=noisy[start+j];  //get the amplitude in time domain
                temp = temp+ Math.pow(segment[j],2);  //get the power spectrum
            }
            Seg[i]=temp*(1/W); //average of power spectrum
            start= (int) (start+W-1);
        }
        return Seg;
    }

    private static double maxValue(double[] array) {
        double max;
        max=array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i]>max){
             max=array[i];
            }
        }
        return max;
    }

    private static double minValue(double[] array) {
        double min;
        min=array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i]<min){
                min=array[i];
            }
        }
        return min;
    }

    public static double[] VAD20(double[] signal)  {

        int L=signal.length;
        double Tseg = 0.025;
        double Tstep = 0.025;
        double fs=16000;
        double T_seg = L/fs;
        double Nseg = Tseg*fs; //Tseg = Nseg/fs (so Tseg = 256/16k = 16ms)
        double Nstep = Tstep*fs; //Tseg = Nseg/fs (so Tseg = 256/16k = 16ms)
        double rstep = Tseg/Tstep;
        double rseg = 1 ; //0.6*(Tseg/Tseg);

        double rEmin = 1+0.01*rseg*rstep;
        double rEmax = 1+0.1*rseg*rstep;
        double rE1= rEmin;

        //for the PST
        double Tps = 1;
        double Lps = Math.round(Tps/Tstep); //Lps=Tps/Tstep, wkt Tstep=Tseg, Tstep = 32ms, Therefpre Lps = 1s/32msec = 30
        double D0 = 0.2;
        double D1 = D0; //delta0 and delta1
        double NB=16;
        double Ymax = Math.pow(2,2*(NB-1));

        Seg = new double[signal.length];
        Ym = new double[signal.length];
        double[] Yms = new double[signal.length];
        double[] PN=null,alphaY=null,alpha_lmda_Th=null,b_lmda=null,Thps=null,rE=null,rE0=null,check=null;
        double[] Th_lmda=null,VAD=null,LE=null;
        double rE_lmda,Nv,rE11,max,min;
        double[] PST=null;
        double[] By=null,value=null,Yms_m=null,temp_By=null;
        double prev_rE_noise = Yms[1];
        double prev_rE_segment = 1;

        double mavg = (0.5/Tseg);
        double temp=0;

        double N=Math.round(L/Nseg); //number of segments in time domain
        Ym = segment(signal,Nseg); //Ym=average power spectrum

        //Step 2 : Initializations

        Yms[0]=Ym[0];
        Th_lmda[0] = Yms[0];
        VAD[0]=0;
        LE[0]=Yms[0];

        for (int m=0;m<2;m++)
        {
            PN[m] =Math.min(Math.max((log(Th_lmda[1]) / log(Ymax)), 0), 1); //---eq 18

            alphaY[m] =1 - (D0 + (D1 * (1 - PN[1])) * rseg * rstep);
            alpha_lmda_Th[m] =alphaY[m];

            b_lmda[m] =Math.min(Math.max((1.3 - 0.5 * PN[1]), 1), 1.3); //----eq 22
            Thps[m] =Math.min(Math.max((2 - PN[m]), 1), 2); //----eq 22
            Th_lmda[m] = alpha_lmda_Th[1] * Th_lmda[1] + (1 - alpha_lmda_Th[1]) * b_lmda[1] * Yms[1];
            rE[m] =rE1;
            rE0[m]=rE[m];
        }

        for (int m=0;m<Lps;m++){
            By[m]=1;
        }
        By[0]=Yms[1];
        int flag13=0;
        int flag10=0;
        value[0] = 1;


        for (int m=0; m<Ym.length;m++) {
            if (m <= mavg) {
                for (int i = 0; i < mavg; i++) {
                    temp = temp + Ym[i];
                }
                Yms_m[m] = temp / m;
            } else {
                for (int i = 0; i < mavg; i++) {
                    temp = temp + Ym[m - i];
                }
                Yms_m[m] = temp / mavg;
            }
        }

        check[0]=0;
        Yms[0]=Yms_m[0];

            // Step 3 : Frame by frame analysis - VAD Decision

        for (int m=1;m<N;m++) {
            Yms[m] = alphaY[m] * Yms[m - 1] + (1 - alphaY[m]) * Yms_m[m]; //recursive smoothing of input power spectrum

            if (m <= Lps)
            {
                    for (int i=0;i<m;i++){
                        temp_By[i]=Yms[i];
                        if (temp_By[i]<1)
                        {
                            temp_By[i]=1;
                        }
                        else if (temp_By[i]>Ymax)
                        {
                            temp_By[i]=Ymax;
                        }
                    }
                value[m] = maxValue(temp_By)/minValue(temp_By);
            } else {
                for (int i=0;i<Lps;i++){
                    By[i]=Yms[m-i];
                    if (By[i]<1)
                    {
                        By[i]=1;
                    }
                    else if (By[i]>Ymax)
                    {
                        By[i]=Ymax;
                    }
                }
                value[m] = maxValue(temp_By)/minValue(temp_By);
            }

            //--update buffer every segment for PST
            if (value[m] <= Thps[m]) { //--PST
                PST[m] = 1;
            } else {
                PST[m] = 0;
            }

            if ((PST[m - 1] == 0) & (PST[m] == 1) & (VAD[m - 1] == 1)) {//%--equation 10
                Th_lmda[m] = b_lmda[m] * Yms[m];
                LE[m] = Yms[m];
                flag10 = 1;
            }

            //-check equation 12
            if (flag10 != 1) {
                if (Yms[m] > LE[m - 1]) {
                    LE[m] = rE[m] * LE[m - 1];
                    check[m]=1;
                } else {
                    LE[m] = Yms[m];
                    check[m]=0;
                }
            }


            //--check equation 13 , if satisfied update Th_lmda to LE else retain previous value
            if ((VAD[m - 1] == 1) & ((LE[m] > LE[m - 1]) & (LE[m - 1] <= LE[m - 2]))) {
                Th_lmda[m] = LE[m];
                flag13 = 1;
            }

            //-- obtain VAD decision from eq 5

            if ((Yms[m] > Th_lmda[m]) | (flag13 == 1)) { //if cond 13 or 5 is satisfied VAD decision is set to 1
                VAD[m] = 1;
            } else {
                VAD[m] = 0;
            }

            //--conditionally update on start or stop of speech utterence
            if ((VAD[m] == 1) & (VAD[m - 1] == 0)) {
                rE_lmda = Yms[m - 1] / prev_rE_noise;
                Nv = m - prev_rE_segment;
                rE0[m + 1] = Math.pow(rE_lmda, (1 / Nv));
                rE1 = Math.max(rEmin, rE0[m + 1]);
                rE11 = rEmin + (rEmax - rEmin) * (1 - PN[m]);
                rE[m + 1] = Math.min(rE1, rE11);

                prev_rE_noise = Yms[m - 1];
                prev_rE_segment = m;

                PN[m + 1] = PN[m];

                alphaY[m + 1] = alphaY[m];
                alpha_lmda_Th[m + 1] = alpha_lmda_Th[m];

                Thps[m + 1] = Thps[m];
                b_lmda[m + 1] = b_lmda[m]; //----eq 22

                Th_lmda[m + 1] = Th_lmda[m];

                LE[m + 1] = LE[m];

            } else if ((VAD[m] == 0) & (VAD[m - 1] == 1)) {

                PN[m + 1] = Math.min(Math.max((Math.log10(Th_lmda[m]) / Math.log10(Ymax)), 0), 1); //---eq 18

                alphaY[m + 1] = 1 - (D0 + (D1 * (1 - PN[m + 1]))) * rseg * rstep;
                alpha_lmda_Th[m + 1] = alphaY[m + 1];

                b_lmda[m + 1] = Math.max((value[m] - 0.5 * PN[m + 1]), 1); //----eq 22
                Thps[m + 1] = 2 - PN[m + 1]; //----eq 22

                Th_lmda[m + 1] = alpha_lmda_Th[m] * Th_lmda[m] + (1 - alpha_lmda_Th[m]) * b_lmda[m] * Yms[m];
                rE[m + 1] = rE1;
                rE0[m + 1] = rE[m];

                LE[m + 1] = LE[m];
            }

            //--conditional updates on speech presence or absence
            if ((VAD[m] == 0) & (VAD[m - 1] != 1)) {
                PN[m + 1] = Math.min(Math.max((Math.log10(Th_lmda[m]) / Math.log10(Ymax)), 0), 1); //---eq 18

                alphaY[m + 1] = 1 - (D0 + (D1 * (1 - PN[m + 1]))) * rseg * rstep;
                alpha_lmda_Th[m + 1] = alphaY[m + 1];

                b_lmda[m + 1] = Math.max((value[m] - 0.5 * PN[m + 1]), 1); //----eq 22
                // b_lmda(m+1) = min(max((value(m)-0.5*PN(m+1)),1);
                Thps[m + 1] = 2 - PN[m + 1]; //----eq 22

                Th_lmda[m + 1] = alpha_lmda_Th[m] * Th_lmda[m] + (1 - alpha_lmda_Th[m]) * b_lmda[m] * Yms[m];
                rE[m + 1] = rE[m];
                rE0[m + 1] = rE[m];

                LE[m + 1] = LE[m];
            } else if ((VAD[m] == 1) & (VAD[m - 1] != 0)) {
                PN[m + 1] = PN[m];

                alphaY[m + 1] = alphaY[m];
                alpha_lmda_Th[m + 1] = alpha_lmda_Th[m];

                Thps[m + 1] = Thps[m];
                b_lmda[m + 1] = b_lmda[m]; //----eq 22

                if (flag13 != 1) {
                    Th_lmda[m + 1] = Th_lmda[m];
                } else {
                    Th_lmda[m + 1] = LE[m];
                }
                rE[m + 1] = rE[m];
                rE0[m + 1] = rE[m];

                LE[m + 1] = LE[m];
            }

            if (flag10==1){
                LE[m+1]=LE[m];}
            //Th_lmda(m+1)=b_lmda(m)*Yms(m);

            flag13=0; //reset eq 13 flag
            flag10=0; //reset eq 13 flag

        }
        return check;
        }
    }
