package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.base;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import butterknife.Unbinder;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.MyApplication;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.application.ApplicationComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity.ActivityComponent;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.di.presentation.activity.ActivityModule;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public abstract class BaseActivity extends AppCompatActivity {

    Unbinder unbinder;

    public ActivityComponent getPresentationComponent() {
        return getApplicationComponent().newPresentationComponent(
                new ActivityModule(this));
    }

    private ApplicationComponent getApplicationComponent() {
        return ((MyApplication) getApplication()).getApplicationComponent();
    }

    protected abstract void setup() throws IOException;

    public void setUnBinder(Unbinder unBinder) {
        this.unbinder = unBinder;
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
