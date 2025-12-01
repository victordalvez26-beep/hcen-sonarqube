package uy.edu.tse.hcen.manager;

import static uy.edu.tse.hcen.config.AppConfig.AUTH_SESSION_URL;
import static uy.edu.tse.hcen.config.AppConfig.SESSION_STATUS_URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;

public class SessionManager {

    private static final String KEY_JWT = "jwt_token";
    private static final String KEY_LOGIN_STATE = "login_state";

    public static void saveJWT(Context context, String jwt) {
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
        prefs.edit()
                .putString(KEY_JWT, jwt)
                .apply();
    }

    public static void getJwtFromBackend(Context context, String token, Consumer<Boolean> onResult) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                JSONObject json = new JSONObject();
                json.put("token", token);

                URL url = new URL(AUTH_SESSION_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder builder = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                }

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject jsonResponse = new JSONObject(builder.toString());
                    String jwt = jsonResponse.optString("jwt", null);
                    if (jwt != null) {
                        saveJWT(context, jwt);
                        onResult.accept(true);
                    } else {
                        Log.w("AuthService", "Respuesta sin campo JWT");
                        onResult.accept(false);
                    }
                } else {
                    Log.e("AuthService", "Error en respuesta del backend. Código: " + responseCode);
                    onResult.accept(false);
                }
            } catch (Exception e) {
                Log.e("AuthService", "Fallo en la petición JWT", e);
                onResult.accept(false);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    public interface SessionCheckCallback {
        void onSessionValid();
        void onSessionInvalid();
        void onNetworkError();
    }

    public static void checkSessionAndProceed(Context context, SessionCheckCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(SESSION_STATUS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Cookie", "hcen_session=" + getJwtSession(context));
                conn.connect();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                JSONObject response = new JSONObject(sb.toString());
                boolean authenticated = response.optBoolean("authenticated", false);

                if (authenticated) {
                    ((android.app.Activity) context).runOnUiThread(callback::onSessionValid);
                } else {
                    ((android.app.Activity) context).runOnUiThread(callback::onSessionInvalid);
                }
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(callback::onNetworkError);
            }
        }).start();
    }

    public static String getJwtSession(Context context) {
        return SecurePrefsHelper.getSessionPrefs(context).getString(KEY_JWT, null);
    }

    public static boolean isLoggedIn(Context context) {
        return getJwtSession(context) != null;
    }

    public static void clearSession(Context context) {
        SecurePrefsHelper.getSessionPrefs(context).edit().clear().apply();
    }

    public static void saveState(Context context, String state) {
        SecurePrefsHelper.getSessionPrefs(context).edit().putString(KEY_LOGIN_STATE, state).apply();
    }

    public static String getState(Context context) {
        return SecurePrefsHelper.getSessionPrefs(context).getString(KEY_LOGIN_STATE, null);
    }

    public static void clearState(Context context) {
        SecurePrefsHelper.getSessionPrefs(context).edit().remove(KEY_LOGIN_STATE).apply();
    }
}