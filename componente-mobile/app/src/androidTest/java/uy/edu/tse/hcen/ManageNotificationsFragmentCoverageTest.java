package uy.edu.tse.hcen;

import static org.junit.Assert.*;

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

import uy.edu.tse.hcen.fragments.ManageNotificationsFragment;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class ManageNotificationsFragmentCoverageTest {
    private Context context;

    @Before
    public void setUpEach() {
        context = ApplicationProvider.getApplicationContext();
        SessionManager.saveJWT(context, "jwt-token");
    }

    @Test
    public void testSwitchEdgeCasesAndBackendErrors() {
        FragmentScenario<ManageNotificationsFragment> scenario =
                FragmentScenario.launchInContainer(ManageNotificationsFragment.class, null, R.style.Theme_HCEN);
        scenario.moveToState(Lifecycle.State.RESUMED);
        SystemClock.sleep(500);
        scenario.onFragment(fragment -> {
            SwitchCompat switchAll = fragment.requireView().findViewById(R.id.switchAll);
            SwitchCompat switchResults = fragment.requireView().findViewById(R.id.switchResults);
            SwitchCompat switchNewAccessRequest = fragment.requireView().findViewById(R.id.switchNewAccessRequest);
            SwitchCompat switchMedicalHistory = fragment.requireView().findViewById(R.id.switchMedicalHistory);
            SwitchCompat switchNewAccessHistory = fragment.requireView().findViewById(R.id.switchNewAccessHistory);
            SwitchCompat switchMaintenance = fragment.requireView().findViewById(R.id.switchMaintenance);
            SwitchCompat switchNewFeatures = fragment.requireView().findViewById(R.id.switchNewFeatures);

            // Apaga todos los individuales para activar all_disabled
            switchResults.setChecked(false);
            switchNewAccessRequest.setChecked(false);
            switchMedicalHistory.setChecked(false);
            switchNewAccessHistory.setChecked(false);
            switchMaintenance.setChecked(false);
            switchNewFeatures.setChecked(false);
            SystemClock.sleep(200);
            assertTrue(switchAll.isChecked());

            // Prende uno individual para desactivar all_disabled
            switchResults.setChecked(true);
            SystemClock.sleep(200);
            assertFalse(switchAll.isChecked());
        });
    }

    @Test
    public void testLoadNotificationPreferencesFromBackendError() {
        // Simula sesión inválida
        SessionManager.clearSession(context);
        FragmentScenario<ManageNotificationsFragment> scenario =
                FragmentScenario.launchInContainer(ManageNotificationsFragment.class, null, R.style.Theme_HCEN);
        scenario.moveToState(Lifecycle.State.RESUMED);
        SystemClock.sleep(500);
        // No crash esperado
    }
}
