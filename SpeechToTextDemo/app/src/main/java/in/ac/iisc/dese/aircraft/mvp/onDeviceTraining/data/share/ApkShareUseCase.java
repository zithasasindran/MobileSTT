package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.share;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextContract;
import io.reactivex.Single;

public class ApkShareUseCase implements SpeechToTextContract.ApkShareUseCaseHelper {

    private final Context context;

    public ApkShareUseCase(Context context) {
        this.context = context;
    }

    @Override
    public Single<File> getApkFile() {
        return Single.just(prepareApkFile());
    }

    private File prepareApkFile() {
        ApplicationInfo app = context.getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;

        // Append file and send Intent
        File originalApk = new File(filePath);
        //Make new directory in new location
        File tempFile = new File(context.getExternalCacheDir() + "/ExtractedApk");
        //If directory doesn't exists create new
        if (!tempFile.isDirectory())
            tempFile.mkdirs();
        //Get application's name and convert to lowercase
        tempFile = new File(tempFile.getPath() + "/" + "speechtotextdemo" + ".apk");

        //If file doesn't exists create new
        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            //Copy file to new location
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            //Open share dialog
        } catch (IOException ignore) {
        }
        return tempFile;
    }
}
