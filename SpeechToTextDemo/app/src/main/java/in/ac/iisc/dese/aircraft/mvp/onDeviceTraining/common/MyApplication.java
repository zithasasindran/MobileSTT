package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.BuildConfig;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application.ApplicationComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application.ApplicationModule;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application.DaggerApplicationComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util.NotLoggingTree;

import timber.log.Timber;

public class MyApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        setupLeakCanary();
        setupTimber();

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void setupLeakCanary() {

    }

    private void setupTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new NotLoggingTree());
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
