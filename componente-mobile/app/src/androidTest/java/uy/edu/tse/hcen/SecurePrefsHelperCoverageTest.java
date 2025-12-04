package uy.edu.tse.hcen;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public class SecurePrefsHelperCoverageTest {

    private static final String GET_ENCRYPTED_PREFS = "getEncryptedPrefs";
    private static final String EMPTY = "empty";
    private static final String EDGE = "edge!@#";

    @Test
    public void testGetEncryptedPrefsReturnsDefaultSharedPrefsOnException() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        // Usar un prefsName inválido para forzar excepción interna (nombre muy largo)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) sb.append('a');
        String longName = sb.toString();
        Method method = SecurePrefsHelper.class.getDeclaredMethod(GET_ENCRYPTED_PREFS, Context.class, String.class);
        method.setAccessible(true);
        SharedPreferences prefs = (SharedPreferences) method.invoke(null, context, longName);
        // Si cae en el catch, devuelve PreferenceManager.getDefaultSharedPreferences(context)
        assertNotNull(prefs);
        prefs.edit().putString("fallback", "ok").commit();
        assertEquals("ok", prefs.getString("fallback", null));
    }
    @Test
    public void testConstructorCoverage() throws Exception {
        Constructor<SecurePrefsHelper> constructor = SecurePrefsHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        SecurePrefsHelper helper = constructor.newInstance();
        assertNotNull(helper);
    }

    @Test
    public void testGetEncryptedPrefsWithDifferentNames() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        Method method = SecurePrefsHelper.class.getDeclaredMethod(GET_ENCRYPTED_PREFS, Context.class, String.class);
        method.setAccessible(true);
        SharedPreferences prefs1 = (SharedPreferences) method.invoke(null, context, "prefs1");
        SharedPreferences prefs2 = (SharedPreferences) method.invoke(null, context, "prefs2");
        assertNotNull(prefs1);
        assertNotNull(prefs2);
        prefs1.edit().putString("a", "1").commit();
        prefs2.edit().putString("b", "2").commit();
        assertEquals("1", prefs1.getString("a", null));
        assertEquals("2", prefs2.getString("b", null));
    }

    @Test
    public void testGetEncryptedPrefsNullName() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        Method method = SecurePrefsHelper.class.getDeclaredMethod(GET_ENCRYPTED_PREFS, Context.class, String.class);
        method.setAccessible(true);
        SharedPreferences prefs = (SharedPreferences) method.invoke(null, context, (String) null);
        assertNotNull(prefs);
    }

    @Test
    public void testGetEncryptedPrefsFallback() throws Exception {
        // Simula error forzando un contexto inválido (null) para forzar fallback
        Method method = SecurePrefsHelper.class.getDeclaredMethod(GET_ENCRYPTED_PREFS, Context.class, String.class);
        method.setAccessible(true);
        try {
            method.invoke(null, null, "prefs");
            fail("Should throw exception");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testSessionPrefsEdgeCases() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
        assertNotNull(prefs);
        prefs.edit().putString("", "emptyKey").commit();
        assertEquals("emptyKey", prefs.getString("", null));
        prefs.edit().putString(null, "nullKey").commit(); // Should not crash
        prefs.edit().putString("special!@#", "val").commit();
        assertEquals("val", prefs.getString("special!@#", null));
    }

    @Test
    public void testPushAndUserNotificationPrefsEdgeCases() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences pushPrefs = SecurePrefsHelper.getPushPrefs(context);
        SharedPreferences notifPrefs = SecurePrefsHelper.getUserNotificationPrefs(context);
        assertNotNull(pushPrefs);
        assertNotNull(notifPrefs);
        pushPrefs.edit().putString("", EMPTY).commit();
        notifPrefs.edit().putString("", EMPTY).commit();
        assertEquals(EMPTY, pushPrefs.getString("", null));
        assertEquals(EMPTY, notifPrefs.getString("", null));
        pushPrefs.edit().putString("null", null).commit(); // Should not crash
        notifPrefs.edit().putString("null", null).commit(); // Should not crash
        pushPrefs.edit().putString(EDGE, "val").commit();
        notifPrefs.edit().putString(EDGE, "val").commit();
        assertEquals("val", pushPrefs.getString(EDGE, null));
        assertEquals("val", notifPrefs.getString(EDGE, null));
    }
}
