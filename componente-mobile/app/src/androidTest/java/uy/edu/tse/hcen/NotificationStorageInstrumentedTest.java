package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uy.edu.tse.hcen.notifications.NotificationItem;
import uy.edu.tse.hcen.notifications.NotificationStorage;

@RunWith(AndroidJUnit4.class)
public class NotificationStorageInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotificationStorage.clear(context);
    }

    @Test
    public void notificationItemFormatsDateUsingLocale() {
        long timestamp = 1_695_000_000_000L;
        NotificationItem item = new NotificationItem("Mensaje", timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        assertEquals(sdf.format(new Date(timestamp)), item.getFormattedDate());
    }

    @Test
    public void saveAndRetrieveNotifications() {
        NotificationStorage.saveNotification(context, "Hola mundo");
        List<NotificationItem> items = NotificationStorage.getNotifications(context);
        assertEquals(1, items.size());
        NotificationItem item = items.get(0);
        assertEquals("Hola mundo", item.getMessage());
        assertTrue(item.getTimestamp() > 0);
    }

    @Test
    public void clearRemovesAllNotifications() {
        NotificationStorage.saveNotification(context, "Hola");
        NotificationStorage.saveNotification(context, "Chau");
        NotificationStorage.clear(context);
        assertTrue(NotificationStorage.getNotifications(context).isEmpty());
    }
}


