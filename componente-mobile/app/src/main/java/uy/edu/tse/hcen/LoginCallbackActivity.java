package uy.edu.tse.hcen;

import static uy.edu.tse.hcen.notifications.MyFirebaseMessagingService.sendTokenFCMBackend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.manager.UserManager;
import uy.edu.tse.hcen.splash.SplashActivity;

import androidx.fragment.app.Fragment;

public class LoginCallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();

        if (data != null) {
            String token = data.getQueryParameter("token");
            String state = data.getQueryParameter("state");

            String expectedState = SessionManager.getState(this);

            if (token != null && state != null && state.equals(expectedState)) {
                SessionManager.clearState(this);

                SessionManager.getJwtFromBackend(this, token, success -> {
                    if (success) {
                        String jwt = SessionManager.getJwtSession(this);

                        Log.e("LCA", "jwt: " + jwt);

                        // Envia TokenFCM para posterior recepción de notificaciones
                        sendTokenFCMBackend(this);

                        // Crear usuario a partir de datos del backend
                        UserManager.createUser(this);

                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Log.e("LoginCallbackActivity", "Error intercambiando Token-JWT");
                        fallbackToSplash();
                    }
                });

                return;
            }
        }

        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void fallbackToSplash() {
        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(
                DialogType.ERROR,
                "Ocurrió un error al autenticarte. Intenta nuevamente"
        );
        errorDialog.show(getSupportFragmentManager(), "errorDialog");

        // Esperar unos segundos antes de redirigir
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }, 3000);
    }
}

