package uy.edu.tse.hcen;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import uy.edu.tse.hcen.notifications.NotificationItem;
import uy.edu.tse.hcen.notifications.NotificationStorage;

@RunWith(AndroidJUnit4.class)
public class NotificationStorageCoverageTest {

    private static final String NOTIFICATIONS_LIST = "notifications_list";

    @Test
    public void saveNotificationForcesJSONException() {
        // Forzar JSONException pasando un SharedPreferences corrupto
        android.content.SharedPreferences prefs = context.getSharedPreferences("push_notifications_prefs", Context.MODE_PRIVATE);
        // Guardar un valor que no es un array JSON
        prefs.edit().putString(NOTIFICATIONS_LIST, "{notAnArray}").apply();
        // Esto forzará JSONException en saveNotification
        NotificationStorage.saveNotification(context, "test");
        // No debe crashear
    }
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotificationStorage.clear(context);
    }

    @Test
    public void deleteNotificationRemovesCorrectItem() throws Exception {
    NotificationStorage.saveNotification(context, "A");
    Thread.sleep(2); // Asegura timestamps distintos
    NotificationStorage.saveNotification(context, "B");
    List<NotificationItem> items = NotificationStorage.getNotifications(context);
    assertEquals(2, items.size());
    long tsToDelete = items.get(0).getTimestamp();
    NotificationStorage.deleteNotification(context, tsToDelete);
    List<NotificationItem> after = NotificationStorage.getNotifications(context);
    assertEquals(1, after.size());
    assertEquals("B", after.get(0).getMessage());
    }

    @Test
    public void getNotificationsHandlesCorruptJsonAndMissingFields() {
        // Corrupt JSON
        android.content.SharedPreferences prefs = context.getSharedPreferences("push_notifications_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString(NOTIFICATIONS_LIST, "notjson").apply();
        List<NotificationItem> items = NotificationStorage.getNotifications(context);
        assertTrue(items.isEmpty());
        // Missing fields
        prefs.edit().putString(NOTIFICATIONS_LIST, "[{\"message\":\"X\"}]").apply();
        try {
            NotificationStorage.getNotifications(context);
        } catch (Exception e) {
            // Should not crash
            fail("Should handle missing timestamp");
        }
    }

    @Test
    public void saveNotificationHandlesEmptyAndNullMessage() {
    NotificationStorage.saveNotification(context, "");
    NotificationStorage.saveNotification(context, null); // No se guarda
    List<NotificationItem> items = NotificationStorage.getNotifications(context);
    assertEquals(1, items.size());
    assertEquals("", items.get(0).getMessage());
    }
}
