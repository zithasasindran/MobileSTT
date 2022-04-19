package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity;

import dagger.Subcomponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextMainActivity;

@Subcomponent(modules = {ActivityModule.class, SpeechRecognitionModule.class, ApkShareModule.class})
public interface ActivityComponent {

    void inject(SpeechToTextMainActivity speechToTextMainActivity);
}
