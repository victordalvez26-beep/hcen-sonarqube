package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class LoginCallbackActivityInstrumentedTest extends BaseInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void successfulCallbackStoresJwt() {
        SessionManager.saveState(context, "abc");
        TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 200, "{\"jwt\":\"jwt-value\"}");
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LoginCallbackActivity.class);
        intent.setData(Uri.parse("hcen://callback?token=tkn&state=abc"));
        ActivityScenario<LoginCallbackActivity> scenario = ActivityScenario.launch(intent);
        SystemClock.sleep(500);
        assertEquals("jwt-value", SessionManager.getJwtSession(context));
        scenario.moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void invalidStateRedirectsToSplash() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LoginCallbackActivity.class);
        intent.setData(Uri.parse("hcen://callback?token=tkn&state=invalid"));
        ActivityScenario<LoginCallbackActivity> scenario = ActivityScenario.launch(intent);
        scenario.moveToState(Lifecycle.State.DESTROYED);
        assertNull(SessionManager.getJwtSession(context));
    }
}


