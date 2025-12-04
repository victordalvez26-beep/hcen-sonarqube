package uy.edu.tse.hcen;

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.widget.Button;
import android.widget.VideoView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.splash.SplashActivity;

@RunWith(AndroidJUnit4.class)
public class SplashActivityExtraInstrumentedTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SecurePrefsHelper.getSessionPrefs(context).edit().clear().commit();
        SessionManager.clearSession(context);
    }

    @Test
    public void splashActivityLoginButtonAndVideo() {
        ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class);
        scenario.onActivity(activity -> {
            Button btnLogin = activity.findViewById(R.id.btnLogin);
            assertNotNull(btnLogin);
            btnLogin.performClick();
            VideoView video = activity.findViewById(R.id.videoBackground);
            assertNotNull(video);
        });
    }

    @Test
    public void splashActivityLifecycleResumeClose() {
        ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class);
        // En Splash, la actividad puede terminar o navegar rápidamente; no forzar RESUMED
        scenario.onActivity(activity -> {
            assertNotNull(activity);
            // Pequeña espera para permitir ciclo de vida inicial sin hacer frágil el test
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // sincronización del test, continuar
            }
        });
        scenario.close();
    }
}
