package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.base;

import android.view.View;

import androidx.fragment.app.Fragment;

import butterknife.Unbinder;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.MyApplication;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application.ApplicationComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.fragment.FragmentComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.fragment.FragmentModule;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public abstract class BaseFragment extends Fragment {

    Unbinder unbinder;

    public FragmentComponent getPresentationComponent() {
        return getApplicationComponent().newFragmentComponent(new FragmentModule(this));
    }

    private ApplicationComponent getApplicationComponent() {
        return ((MyApplication) requireActivity().getApplication()).getApplicationComponent();
    }

    protected abstract void setup(View view);

    public void setUnBinder(Unbinder unBinder) {
        this.unbinder = unBinder;
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }
}
