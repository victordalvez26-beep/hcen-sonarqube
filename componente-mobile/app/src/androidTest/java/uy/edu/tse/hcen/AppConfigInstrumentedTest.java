package uy.edu.tse.hcen;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.config.AppConfig;

@RunWith(AndroidJUnit4.class)
public class AppConfigInstrumentedTest {
    @Test
    public void allConstantsAreAccessible() {
        assertNotNull(AppConfig.BACKEND_URL);
        assertNotNull(AppConfig.FRONTEND_URL);
        assertNotNull(AppConfig.LOGOUT_URL);
        assertNotNull(AppConfig.AUTH_SESSION_URL);
        assertNotNull(AppConfig.USER_DATA_URL);
        assertNotNull(AppConfig.REGISTER_DEVICE_TOKEN_URL);
        assertNotNull(AppConfig.MANAGE_NOTIFICATIONS_URL);
        assertNotNull(AppConfig.SUMMARY_URL);
        assertNotNull(AppConfig.DOCS_URL);
        assertNotNull(AppConfig.HISTORY_PDF_URL);
        assertNotNull(AppConfig.AUTH_GUBUY_URL);
        assertNotNull(AppConfig.CLIENT_ID_GUBUY);
        assertNotNull(AppConfig.REDIRECT_URI);
        assertNotNull(AppConfig.SCOPE_GUBUY);
        assertNotNull(AppConfig.ID_DIGITAL);
        assertNotNull(AppConfig.PREFS_DATA);
        assertNotNull(AppConfig.PREFS_PUSH_NOTIFICATIONS);
        assertNotNull(AppConfig.PREFS_USER_NOTIFICATIONS);
        assertNotNull(AppConfig.PREFS_TOKEN_FCM);
        assertTrue(AppConfig.LOGOUT_URL.contains(AppConfig.BACKEND_URL));
    }
}