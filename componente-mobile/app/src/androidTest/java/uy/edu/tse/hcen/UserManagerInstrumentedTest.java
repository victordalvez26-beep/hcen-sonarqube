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

    private static final String TEST_EMAIL = "a@b.com";
    private static final String TEST_DEPARTMENT = "MONTEVIDEO";

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
    UserManager.saveUser(context, TEST_EMAIL, TEST_DEPARTMENT, "Loc", "Addr", () -> latch.countDown());
        latch.await(); // Wait for saveUser to complete
        User u = UserManager.getUser(context);
        // getUser spawns background fetch but returns stored prefs; ensure saved values are present
    assertEquals(TEST_EMAIL, u.getEmail());
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
                .put("departamento", TEST_DEPARTMENT);

        TestNetworkDispatcher.enqueueResponse(AppConfig.USER_DATA_URL, 200, payload.toString());

        // Ejecutar y esperar a que los datos se escriban en SharedPreferences
        Method m = UserManager.class.getDeclaredMethod("fetchUserDataFromBackend", android.content.Context.class, String.class);
        m.invoke(null, context, "jwt-xyz");

        // Esperar hasta que el valor de email esté presente (máx 2 segundos)
        long start = System.currentTimeMillis();
        String email = null;
        while (System.currentTimeMillis() - start < 2000) {
            email = UserManager.getUser(context).getEmail();
            if (email != null) break;
            Thread.sleep(50);
        }
        assertEquals("net@host", email);
        assertEquals("Juan", UserManager.getUser(context).getFirstName());
    }

    @Test
    public void clearUserClearsPrefs() {
        UserManager.saveUser(context, TEST_EMAIL, TEST_DEPARTMENT, "Loc", "Addr", () -> {});
        UserManager.clearUser(context);
        User u = UserManager.getUser(context);
        assertNull(u.getEmail());
    }
}
