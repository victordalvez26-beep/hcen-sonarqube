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
    public void splashActivity_loginButtonAndVideo() {
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
    public void splashActivity_lifecycleResumeClose() {
        ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
        scenario.close();
    }
}
