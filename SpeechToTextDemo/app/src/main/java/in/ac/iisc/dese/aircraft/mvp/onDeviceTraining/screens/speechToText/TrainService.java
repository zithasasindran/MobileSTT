package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;

public class TrainService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        //TRAIN THREAD
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
//                        File makefiledir = new File(Train.path);
//                        // have the object build the directory structure, if needed.
//                        makefiledir.mkdirs();

                    if (Train.isCSVFull()) {
                        System.out.println("TRAINING HAS STARTED");
                        { //GUI BLOCK
                            SpeechToTextMainActivity.speechToTextMainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //gui output
                                    Toast.makeText(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext(), "Service Started", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        //STARTING TRAINING
                        Train.trainthemodel();
                    } else {
                        { //GUI BLOCK
                            SpeechToTextMainActivity.speechToTextMainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //gui output
                                    Toast.makeText(SpeechToTextMainActivity.speechToTextMainActivity.getApplicationContext()
                                            , "Please Fill the Dataset Required to Train", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }


                    { //GUI BLOCK
                        SpeechToTextMainActivity.speechToTextMainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //gui output
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        t.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}

