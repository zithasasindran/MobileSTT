package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application;

import javax.inject.Singleton;

import dagger.Component;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity.ActivityComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity.ActivityModule;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.fragment.FragmentComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.fragment.FragmentModule;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    ActivityComponent newPresentationComponent(ActivityModule activityModule);

    FragmentComponent newFragmentComponent(FragmentModule fragmentModule);
}
