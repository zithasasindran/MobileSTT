package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileNotFoundException;

import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion.SpeechToTextData;
import io.reactivex.Single;

public interface SpeechToTextContract {

    interface View {

        void startPulsatorViewAnimation();

        void stopPulsatorViewAnimation();

        void updateFabViewToRecordingState();

        void updateFabViewToStopState();

        void setRecordButtonTag(String tag);

        void bindSpeechDataToRecyclerView(SpeechToTextData speechToTextData);

        void showNoDataFoundText();

        void hideNoDataFoundText();

        void showAudioRecorderInitializationFailedError();

        void showPreparingApkFileToast();

        void shareApkFileReady(File file);

        void clearSpeechToTextDataList();

        void requestAudioRecordingPermission();

        //new
        //void bindSpeechDataToRecyclerView(SpeechToTextData success);
    }

    interface Presenter {

        void registerListener(SpeechToTextContract.View view);

        void unregisterListener();

        void recordFabOnClick(String tag);

        void deleteFabOnClick();

        void shareFabOnClick();

        void audioRecordPermissionGranted();
    }

    interface SpeechRecognitionUseCaseHelper {

        Single<Boolean> initAudioRecorder();

        Single<Boolean> startAudioRecorder();

        Single<String[]> startRecognition() throws FileNotFoundException;

        Single<SpeechToTextData> createSpeechToTextDataModel(String[] convertedText);

        boolean stopAudioRecorder();
    }

    interface ApkShareUseCaseHelper {

        Single<File> getApkFile();
    }
}

