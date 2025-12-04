package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class SessionManagerInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        TestNetworkDispatcher.install();
        TestNetworkDispatcher.clear();
        SessionManager.clearSession(context);
        SessionManager.clearState(context);
    }

    @Test
    public void saveAndRetrieveJwtAndState() {
        SessionManager.saveJWT(context, "jwt-x");
        assertEquals("jwt-x", SessionManager.getJwtSession(context));
        assertTrue(SessionManager.isLoggedIn(context));

        SessionManager.clearSession(context);
        assertNull(SessionManager.getJwtSession(context));
        assertFalse(SessionManager.isLoggedIn(context));

        SessionManager.saveState(context, "state-1");
        assertEquals("state-1", SessionManager.getState(context));
        SessionManager.clearState(context);
        assertNull(SessionManager.getState(context));
    }

    @Test
    public void getJwtFromBackendSuccessAndFailure() throws Exception {
        // success
        TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 200, "{\"jwt\":\"ok-jwt\"}");

        CountDownLatch latch = new CountDownLatch(1);
        SessionManager.getJwtFromBackend(context, "token-123", success -> {
            if (success) latch.countDown();
        });

        boolean awaited = latch.await(2, TimeUnit.SECONDS);
        assertTrue("expected success callback", awaited);
        assertEquals("ok-jwt", SessionManager.getJwtSession(context));

        // failure
        SessionManager.clearSession(context);
        TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 500, "error");
        CountDownLatch latch2 = new CountDownLatch(1);
        SessionManager.getJwtFromBackend(context, "token-456", success -> {
            if (!success) latch2.countDown();
        });
        boolean awaited2 = latch2.await(2, TimeUnit.SECONDS);
        assertTrue("expected failure callback", awaited2);
    }
}
