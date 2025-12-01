package uy.edu.tse.hcen.splash;

import static uy.edu.tse.hcen.config.AppConfig.FRONTEND_URL;
import static uy.edu.tse.hcen.config.AppConfig.ID_DIGITAL;
import static uy.edu.tse.hcen.config.AuthUtils.randomString;
import static uy.edu.tse.hcen.notifications.MyFirebaseMessagingService.getTokenFCM;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import uy.edu.tse.hcen.MainActivity;
import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private VideoView videoBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Solicitar permiso de notificaciones en Android 13+
        if ((android.os.Build.VERSION.SDK_INT >= 33) && (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        super.onCreate(savedInstanceState);

        // Obtiene token para envio de notificaciones personalizadas
        getTokenFCM(this);

        if (SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);
        videoBackground = findViewById(R.id.videoBackground);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> login());

        TextView tvIdDigital = findViewById(R.id.tvIdDigital);
        ImageView btnIdDigital = findViewById(R.id.btnIdDigital);
        View.OnClickListener openIdDigital = v -> openUrl(ID_DIGITAL);
        tvIdDigital.setOnClickListener(openIdDigital);
        btnIdDigital.setOnClickListener(openIdDigital);

        TextView tvHcenWeb = findViewById(R.id.tvHcenWeb);
        ImageView btnHcenWeb = findViewById(R.id.btnHcenWeb);
        View.OnClickListener openHcenWeb = v -> openUrl(FRONTEND_URL);
        tvHcenWeb.setOnClickListener(openHcenWeb);
        btnHcenWeb.setOnClickListener(openHcenWeb);

        setVideoBackground();
    }

    private void login() {
        String state = randomString(16);
        SessionManager.saveState(this, state);

        String authUrl = AppConfig.AUTH_GUBUY_URL +
                "?response_type=code" +
                "&scope=" + Uri.encode(AppConfig.SCOPE_GUBUY) +
                "&client_id=" + AppConfig.CLIENT_ID_GUBUY +
                "&state=" + Uri.encode(state) +
                "&redirect_uri=" + Uri.encode(AppConfig.REDIRECT_URI);

        openUrl(authUrl);
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void setVideoBackground() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_splash_video);
        videoBackground.setVideoURI(uri);

        // Loop del video
        videoBackground.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f); // silencio
            adjustVideoScaling(mp);
            mp.start();
        });
    }

    private void adjustVideoScaling(MediaPlayer mp) {
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;

        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;

        android.view.ViewGroup.LayoutParams lp = videoBackground.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = (int) (screenHeight * videoProportion);
            lp.height = screenHeight;
        } else {
            lp.width = screenWidth;
            lp.height = (int) (screenWidth / videoProportion);
        }

        videoBackground.setLayoutParams(lp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoBackground.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}