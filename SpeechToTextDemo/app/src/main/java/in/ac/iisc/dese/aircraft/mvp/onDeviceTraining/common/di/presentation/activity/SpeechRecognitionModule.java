package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import dagger.Module;
import dagger.Provides;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion.SpeechRecognitionUseCase;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextContract;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.SpeechToTextPresenter;

/*
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
@Module
public class SpeechRecognitionModule {

    @Provides
    TensorFlowInferenceInterface getTensorFlowInferenceInterface(@ActivityScope Context context) { //Interpreter
        //return new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILENAME);
        return null;
    }

    @Provides
    SpeechToTextContract.SpeechRecognitionUseCaseHelper getSpeechRecognitionUseCaseHelper(
            @ActivityScope Context context) {
        return new SpeechRecognitionUseCase(context);
    }

    /*@Provides
    SpeechToTextContract.SpeechRecognitionUseCaseHelper getSpeechRecognitionUseCaseHelper(
            @ActivityScope Context context, AssetManager assetManager,
            TensorFlowInferenceInterface tensorFlowInferenceInterface) throws IOException {
        return new SpeechRecognitionUseCase(assetManager, tensorFlowInferenceInterface);
    }*/

    @Provides
    SpeechToTextContract.Presenter getPresenter(SpeechToTextContract.SpeechRecognitionUseCaseHelper
                                                        speechRecognitionUseCaseHelper,
                                                SpeechToTextContract.ApkShareUseCaseHelper
                                                        apkShareUseCaseHelper) {
        return new SpeechToTextPresenter(speechRecognitionUseCaseHelper, apkShareUseCaseHelper);
    }
}