package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final FragmentActivity activity;

    public ActivityModule(FragmentActivity activity) {
        this.activity = activity;
    }

    @Provides
    Activity getActivity() {
        return activity;
    }

    @Provides
    @ActivityScope
    Context getContext(Activity activity) {
        return activity;
    }

    @Provides
    @ActivityScope
    FragmentManager getFragmentManager() {
        return activity.getSupportFragmentManager();
    }
}
