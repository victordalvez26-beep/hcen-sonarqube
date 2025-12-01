package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;

@RunWith(AndroidJUnit4.class)
public class SecurePrefsHelperInstrumentedTest {

    @Test
    public void encryptedPrefsPersistValues() {
        SharedPreferences sessionPrefs = SecurePrefsHelper.getSessionPrefs(ApplicationProvider.getApplicationContext());
        assertNotNull(sessionPrefs);

        sessionPrefs.edit().putString("key", "value").commit();
        assertEquals("value", sessionPrefs.getString("key", null));
    }
}


