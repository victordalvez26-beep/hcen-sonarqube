package uy.edu.tse.hcen;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;

import java.util.function.Consumer;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;

abstract class BaseInstrumentedTest {

    static {
        TestNetworkDispatcher.install();
    }

    @Before
    public void baseSetUp() {
        TestNetworkDispatcher.clear();
        Context context = ApplicationProvider.getApplicationContext();
        SecurePrefsHelper.getSessionPrefs(context).edit().clear().commit();
        SecurePrefsHelper.getPushPrefs(context).edit().clear().commit();
        SecurePrefsHelper.getUserNotificationPrefs(context).edit().clear().commit();
    }

    protected <F extends Fragment> void withFragmentInHost(F fragment, Consumer<F> block) {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            activity.setFragment(fragment);
            block.accept(fragment);
        });
        scenario.close();
    }
}


