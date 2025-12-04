package uy.edu.tse.hcen;

import static org.junit.Assert.*;

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
public class SessionManagerCoverageTest {
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
    public void getJwtFromBackendNoJwtField() throws Exception {
        TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 200, "{\"notjwt\":\"value\"}");
        CountDownLatch latch = new CountDownLatch(1);
        SessionManager.getJwtFromBackend(context, "token-x", success -> {
            assertFalse(success);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void getJwtFromBackendExceptionBranch() throws Exception {
        // Simula error lanzando excepción (URL inválida)
        TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 200, "{badjson}");
        CountDownLatch latch = new CountDownLatch(1);
        SessionManager.getJwtFromBackend(context, "token-x", success -> {
            assertFalse(success);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
