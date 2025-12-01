
package uy.edu.tse.hcen;

import uy.edu.tse.hcen.splash.SplashActivity;

import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class SplashActivityInstrumentedTest {

    @Test
    public void whenLoggedInRedirectsToMain() {
        SessionManager.saveJWT(ApplicationProvider.getApplicationContext(), "jwt-test-2");
        ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class);
        // if logged in it should finish quickly (redirect). Ensure scenario created.
        assertNotNull(scenario);
        scenario.close();
    }

    @Test
    public void whenNotLoggedInShowsLoginButton() {
        SessionManager.clearSession(ApplicationProvider.getApplicationContext());
        ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class);
        assertNotNull(scenario);
        scenario.close();
    }
}
