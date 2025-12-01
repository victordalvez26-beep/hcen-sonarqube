package uy.edu.tse.hcen.helper;

import static uy.edu.tse.hcen.config.AppConfig.PREFS_DATA;
import static uy.edu.tse.hcen.config.AppConfig.PREFS_PUSH_NOTIFICATIONS;
import static uy.edu.tse.hcen.config.AppConfig.PREFS_USER_NOTIFICATIONS;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SecurePrefsHelper {

    private static SharedPreferences getEncryptedPrefs(Context context, String prefsName) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    prefsName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback to regular SharedPreferences on error (e.g., in tests)
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static SharedPreferences getSessionPrefs(Context context) {
        return getEncryptedPrefs(context, PREFS_DATA);
    }

    public static SharedPreferences getPushPrefs(Context context) {
        return getEncryptedPrefs(context, PREFS_PUSH_NOTIFICATIONS);
    }

    public static SharedPreferences getUserNotificationPrefs(Context context) {
        return getEncryptedPrefs(context, PREFS_USER_NOTIFICATIONS);
    }
}
