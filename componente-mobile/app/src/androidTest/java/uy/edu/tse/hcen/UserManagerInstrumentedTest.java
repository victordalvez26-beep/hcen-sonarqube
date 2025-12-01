package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.logging.Logger;

import java.util.concurrent.CountDownLatch;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.UserManager;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class UserManagerInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        TestNetworkDispatcher.install();
        TestNetworkDispatcher.clear();
        // clear stored user
        UserManager.clearUser(context);
        SessionManager.clearSession(context);
    }

    @Test
    public void saveAndLoadUserFromPrefs() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TestNetworkDispatcher.enqueueResponse(AppConfig.UPDATE_USER_DATA_URL, 200, "{\"success\": true}");
        UserManager.saveUser(context, "a@b.com", "MONTEVIDEO", "Loc", "Addr", () -> latch.countDown());
        latch.await(); // Wait for saveUser to complete
        User u = UserManager.getUser(context);
        // getUser spawns background fetch but returns stored prefs; ensure saved values are present
        assertEquals("a@b.com", u.getEmail());
    }

    @Test
    public void fetchUserDataFromBackendUpdatesPrefs() throws Exception {
        JSONObject payload = new JSONObject()
                .put("email", "net@host")
                .put("primer_nombre", "Juan")
                .put("primer_apellido", "Perez")
                .put("codigo_documento", "1234")
                .put("tipo_documento", "DNI")
                .put("nacionalidad", "UY")
                .put("localidad", "Montevideo")
                .put("direccion", "Calle 1")
                .put("fecha_nacimiento", "1980-05-01")
                .put("departamento", "MONTEVIDEO");

        TestNetworkDispatcher.enqueueResponse(AppConfig.USER_DATA_URL, 200, payload.toString());

        try {
            // call private static fetchUserDataFromBackend via reflection
            Method m = UserManager.class.getDeclaredMethod("fetchUserDataFromBackend", android.content.Context.class, String.class);
            m.setAccessible(true);
            m.invoke(null, context, "jwt-xyz");
        } catch (Exception e) {
            // Log the error for debugging, but do not fail the test
            Logger logger = Logger.getLogger(UserManagerInstrumentedTest.class.getName());
            logger.info("Reflection error in fetchUserDataFromBackend: " + e);
        }

        User u = UserManager.getUser(context);
        // verify some fields
        assertEquals("net@host", u.getEmail());
        assertEquals("Juan", u.getFirstName());
    }

    @Test
    public void clearUser_clearsPrefs() {
    UserManager.saveUser(context, "a@b.com", "MONTEVIDEO", "Loc", "Addr", () -> {});
        UserManager.clearUser(context);
        User u = UserManager.getUser(context);
        assertNull(u.getEmail());
    }
}
