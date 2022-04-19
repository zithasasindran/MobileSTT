package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.fragment;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    private final Fragment fragment;

    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @FragmentScope
    Context getContext() {
        return fragment.requireContext();
    }

    @Provides
    @FragmentScope
    FragmentManager getFragmentManager() {
        return fragment.getChildFragmentManager();
    }
}
