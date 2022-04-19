package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @ApplicationScope
    @Singleton
    @Provides
    Context getContext() {
        return application.getApplicationContext();
    }

    @Singleton
    @Provides
    Handler createMainHandler() {
        return new Handler(Looper.getMainLooper());
    }
}
