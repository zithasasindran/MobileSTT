package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.share.ApkShareUseCase;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextContract;

@Module
public class ApkShareModule {

    @Provides
    SpeechToTextContract.ApkShareUseCaseHelper getApkShareUseCaseHelper(
            @ActivityScope Context context) {
        return new ApkShareUseCase(context);
    }
}
