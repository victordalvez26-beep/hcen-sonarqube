package uy.edu.tse.hcen;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import uy.edu.tse.hcen.fragments.HomeFragment;
import uy.edu.tse.hcen.fragments.NotificationsFragment;
import uy.edu.tse.hcen.fragments.SummaryDigitalFragment;
import uy.edu.tse.hcen.fragments.ProfileFragment;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.splash.SplashActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
            return;
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Configurar toolbar como ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

}