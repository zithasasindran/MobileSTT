package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileNotFoundException;

import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion.SpeechToTextData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SpeechToTextPresenter implements SpeechToTextContract.Presenter {

    private final SpeechToTextContract.SpeechRecognitionUseCaseHelper speechRecognitionUseCaseHelper;
    private final SpeechToTextContract.ApkShareUseCaseHelper apkShareUseCaseHelper;
    private SpeechToTextContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public SpeechToTextPresenter(SpeechToTextContract.SpeechRecognitionUseCaseHelper
                                         speechRecognitionUseCaseHelper,
                                 SpeechToTextContract.ApkShareUseCaseHelper apkShareUseCaseHelper) {
        this.speechRecognitionUseCaseHelper = speechRecognitionUseCaseHelper;
        this.apkShareUseCaseHelper = apkShareUseCaseHelper;
    }

    @Override
    public void registerListener(SpeechToTextContract.View view) {
        this.view = view;
    }

    @Override
    public void unregisterListener() {
        this.view = null;
        compositeDisposable.clear();
    }

    @Override
    public void recordFabOnClick(String tag) {
        if (tag.equals("stop")) {
            this.view.setRecordButtonTag("start");
            this.view.startPulsatorViewAnimation();
            this.view.updateFabViewToRecordingState();
            startAudioRecorder();
        } else {
            this.view.setRecordButtonTag("stop");
            this.view.updateFabViewToStopState();
            stopAudioRecorder();
        }
    }

    @Override
    public void deleteFabOnClick() {
        this.view.clearSpeechToTextDataList();
    }

    @Override
    public void shareFabOnClick() {
        this.view.showPreparingApkFileToast();
        getApkFile();
    }

    @Override
    public void audioRecordPermissionGranted() {
        initAudioRecorder();
    }

    private void initAudioRecorder() {
        compositeDisposable.add(speechRecognitionUseCaseHelper.initAudioRecorder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        if (view != null) {
                            if (!success) {
                                view.showAudioRecorderInitializationFailedError();
                            } else {
                                Timber.d("SpeechToTextDemo, SpeechToTextPresenter, initAudioRecorder onSuccess");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("SpeechToTextDemo, SpeechToTextPresenter, initAudioRecorder onError: %s", e.toString());
                    }
                }));
    }

    private void startAudioRecorder() {
        compositeDisposable.add(speechRecognitionUseCaseHelper.startAudioRecorder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        Timber.d("SpeechToTextDemo, SpeechToTextPresenter, startAudioRecorder onSuccess, calling startRecognition");
                        if (view != null) {
                            view.updateFabViewToStopState();
                            try {
                                startRecognition();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("SpeechToTextDemo, SpeechToTextPresenter, startAudioRecorder onError: %s", e.toString());
                    }
                }));
    }

    private void stopAudioRecorder() {
        if (speechRecognitionUseCaseHelper.stopAudioRecorder()) {
            Timber.d("SpeechToTextDemo, SpeechToTextPresenter, stopAudioRecorder()");
        }
    }

    private void startRecognition() throws FileNotFoundException {
        compositeDisposable.add(speechRecognitionUseCaseHelper.startRecognition()
                .flatMap(speechRecognitionUseCaseHelper::createSpeechToTextDataModel)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<SpeechToTextData>() {
                    @Override
                    public void onSuccess(SpeechToTextData speechToTextData) {
                        Timber.d("SpeechToTextDemo, SpeechToTextPresenter, startRecognition output: %s", speechToTextData.toString());
                        if (view != null) {
                            view.setRecordButtonTag("stop");
                            view.stopPulsatorViewAnimation();
                            view.bindSpeechDataToRecyclerView(speechToTextData);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("SpeechToTextDemo, SpeechToTextPresenter, startRecognition onError: %s", e.toString());
                    }
                }));
    }

    private void getApkFile() {
        compositeDisposable.add(apkShareUseCaseHelper.getApkFile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<File>() {
                    @Override
                    public void onSuccess(File file) {
                        if (view != null) {
                            view.shareApkFileReady(file);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("SpeechToTextDemo, SpeechToTextPresenter, getApkFile() onError: %s", e.toString());
                    }
                }));
    }
}
