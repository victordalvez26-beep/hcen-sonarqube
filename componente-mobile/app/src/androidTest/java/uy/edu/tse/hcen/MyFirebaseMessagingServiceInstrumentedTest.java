package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.notifications.MyFirebaseMessagingService;
import uy.edu.tse.hcen.notifications.NotificationItem;
import uy.edu.tse.hcen.notifications.NotificationStorage;

@RunWith(AndroidJUnit4.class)
public class MyFirebaseMessagingServiceInstrumentedTest {

    private Context context;
    private TestMessagingService service;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotificationStorage.clear(context);
        service = new TestMessagingService();
        service.attach(context);
    }

    @Test
    public void onMessageReceivedPersistsBodyFromDataPayload() {
        RemoteMessage message = new RemoteMessage.Builder("test:123")
                .addData("title", "Titulo")
                .addData("body", "Contenido")
                .build();

        service.onMessageReceived(message);

        NotificationItem stored = NotificationStorage.getNotifications(context).get(0);
        assertEquals("Contenido", stored.getMessage());
    }

    @Test
    public void onNewTokenPersistsInSecurePrefs() {
        service.onNewToken("nuevo_token");
        String storedToken = uy.edu.tse.hcen.helper.SecurePrefsHelper
                .getPushPrefs(context)
                .getString(uy.edu.tse.hcen.config.AppConfig.PREFS_TOKEN_FCM, null);
        assertEquals("nuevo_token", storedToken);
    }

    @Test
    public void sendTokenBackendPostsPayload() {
        uy.edu.tse.hcen.helper.SecurePrefsHelper.getPushPrefs(context)
                .edit().putString(uy.edu.tse.hcen.config.AppConfig.PREFS_TOKEN_FCM, "abc").commit();
        SessionManager.saveJWT(context, "jwt");
        TestNetworkDispatcher.enqueueResponse(AppConfig.REGISTER_DEVICE_TOKEN_URL, 200, "{\"success\":true}");

        MyFirebaseMessagingService.sendTokenFCMBackend(context);
        SystemClock.sleep(500);

        String body = TestNetworkDispatcher.getLastRequestBody(AppConfig.REGISTER_DEVICE_TOKEN_URL);
        assertEquals("{\"deviceToken\":\"abc\"}", body.replace(" ", "").replace("\n", ""));
    }

    private static class TestMessagingService extends MyFirebaseMessagingService {
        void attach(Context ctx) {
            super.attachBaseContext(ctx);
        }
    }
}


