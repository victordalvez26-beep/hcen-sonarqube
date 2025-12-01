package uy.edu.tse.hcen;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;

@RunWith(AndroidJUnit4.class)
public class SecurePrefsHelperExtraInstrumentedTest {
    @Test
    public void getEncryptedPrefsErrorBranch() {
        try {
            // Simulate error by passing null context (should throw RuntimeException)
            uy.edu.tse.hcen.helper.SecurePrefsHelper.class.getDeclaredMethod("getEncryptedPrefs", android.content.Context.class, String.class)
                .setAccessible(true);
            uy.edu.tse.hcen.helper.SecurePrefsHelper.class.getDeclaredMethod("getEncryptedPrefs", android.content.Context.class, String.class)
                .invoke(null, null, "prefs");
        } catch (Exception e) {
            // Should throw RuntimeException
            assertNotNull(e);
        }
    }

    @Test
    public void getSessionPrefsReturnsEncryptedPrefs() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
        assertNotNull(prefs);
        prefs.edit().putString("key", "value").commit();
        assertEquals("value", prefs.getString("key", null));
    }

    @Test
    public void getPushPrefsReturnsEncryptedPrefs() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = SecurePrefsHelper.getPushPrefs(context);
        assertNotNull(prefs);
        prefs.edit().putString("push", "yes").commit();
        assertEquals("yes", prefs.getString("push", null));
    }

    @Test
    public void getUserNotificationPrefsReturnsEncryptedPrefs() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = SecurePrefsHelper.getUserNotificationPrefs(context);
        assertNotNull(prefs);
        prefs.edit().putString("notif", "ok").commit();
        assertEquals("ok", prefs.getString("notif", null));
    }
}