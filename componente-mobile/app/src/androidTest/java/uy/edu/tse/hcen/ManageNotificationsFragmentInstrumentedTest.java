package uy.edu.tse.hcen;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.SystemClock;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.fragments.ManageNotificationsFragment;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class ManageNotificationsFragmentInstrumentedTest extends BaseInstrumentedTest {

    private Context context;

    @Before
    public void setUpEach() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void loadsPreferencesAndTogglesGlobalSwitch() {
        SessionManager.saveJWT(context, "jwt-token");
        String response = "{"
                + "\"notifyResults\":true,"
                + "\"notifyNewAccessRequest\":false,"
                + "\"notifyMedicalHistory\":true,"
                + "\"notifyNewAccessHistory\":false,"
                + "\"notifyMaintenance\":true,"
                + "\"notifyNewFeatures\":true,"
                + "\"allDisabled\":false"
                + "}";
        TestNetworkDispatcher.enqueueResponse(AppConfig.MANAGE_NOTIFICATIONS_URL, 200, response);
        TestNetworkDispatcher.enqueueResponse(AppConfig.MANAGE_NOTIFICATIONS_URL, 200, "{\"success\":true,\"message\":\"ok\"}");

        FragmentScenario<ManageNotificationsFragment> scenario =
                FragmentScenario.launchInContainer(ManageNotificationsFragment.class, null, R.style.Theme_HCEN);
        scenario.moveToState(Lifecycle.State.RESUMED);

        SystemClock.sleep(500);

        scenario.onFragment(fragment -> {
            SwitchCompat switchAll = fragment.requireView().findViewById(R.id.switchAll);
            SwitchCompat switchResults = fragment.requireView().findViewById(R.id.switchResults);

            assertFalse(switchAll.isChecked());
            assertTrue(switchResults.isChecked());

            switchAll.performClick();
            SystemClock.sleep(200);

            assertTrue(switchAll.isChecked());
            assertFalse(switchResults.isChecked());
        });

        String lastBody = TestNetworkDispatcher.getLastRequestBody(AppConfig.MANAGE_NOTIFICATIONS_URL);
        assertTrue(lastBody.contains("\"allDisabled\":true"));
    }

}


