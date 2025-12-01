package uy.edu.tse.hcen.notifications;

import static uy.edu.tse.hcen.config.AppConfig.BACKEND_URL;
import static uy.edu.tse.hcen.config.AppConfig.PREFS_TOKEN_FCM;
import static uy.edu.tse.hcen.config.AppConfig.REGISTER_DEVICE_TOKEN_URL;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uy.edu.tse.hcen.MainActivity;
import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.helper.SecurePrefsHelper;
import uy.edu.tse.hcen.manager.SessionManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = "Notificación";
        String body = null;

        // Si es mensaje de datos
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            title = data.get("title");
            body = data.get("body");
        }

        // Si es mensaje de notificación
        if ((body == null || body.isEmpty()) && remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (body != null && !body.isEmpty()) {
            NotificationStorage.saveNotification(getApplicationContext(), body);
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {
        // Crear canal de notificación (necesario en Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal por defecto";
            String description = "Canal para notificaciones FCM";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Intent para abrir MainActivity al tocar la notificación
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(largeIcon)
                .setContentTitle(title != null ? title : "HCEN")
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)) // para textos largos
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onNewToken(String token) {
        Log.i(TAG, "Nuevo token FCM: " + token);
        SecurePrefsHelper.getPushPrefs(getApplicationContext())
                .edit()
                .putString(PREFS_TOKEN_FCM, token)
                .apply();
        sendTokenFCMBackend(getApplicationContext());
    }

    public static void getTokenFCM(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MyFirebaseMessagingService", "Error al obtener token FCM", task.getException());
                        return;
                    }
                    String tokenFCM = task.getResult();
                    Log.i("MyFirebaseMessagingService", "Token FCM: " + tokenFCM);
                    SecurePrefsHelper.getPushPrefs(context)
                            .edit()
                            .putString(PREFS_TOKEN_FCM, tokenFCM)
                            .apply();
                });
    }

    public static void sendTokenFCMBackend(Context context) {
        String token = SecurePrefsHelper.getPushPrefs(context).getString(PREFS_TOKEN_FCM, null);

        if (token == null || token.isEmpty()) {
            Log.w("FCM", "Token FCM no disponible en SecurePrefs");
            return;
        }

        String jwt = SessionManager.getJwtSession(context);

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(REGISTER_DEVICE_TOKEN_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Authorization", "Bearer " + jwt);
                connection.setDoOutput(true);

                // Construir body JSON
                JSONObject json = new JSONObject();
                json.put("deviceToken", token);
                String body = json.toString();

                // Enviar body
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line.trim());
                    }
                }

                String responseBody = responseBuilder.toString();

                if (responseCode == HttpURLConnection.HTTP_OK && !responseBody.isEmpty()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    boolean success = jsonResponse.optBoolean("success", false);
                    String message = jsonResponse.optString("message", "Sin mensaje");

                    if (success) {
                        Log.i("FCM", "Token FCM registrado correctamente: " + message);
                    } else {
                        Log.e("FCM", "Registro de token fallido: " + message);
                    }
                } else {
                    Log.e("FCM", "Error en respuesta del backend. Código: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("FCM", "Error al enviar token al backend", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}