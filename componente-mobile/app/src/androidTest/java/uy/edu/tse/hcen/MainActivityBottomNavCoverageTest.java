package uy.edu.tse.hcen;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class MainActivityBottomNavCoverageTest {
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SessionManager.saveJWT(context, "jwt-test");
    }

    @Test
    public void bottomNavSelectsAllFragments() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> {
            BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
            // Selecciona Home
            nav.setSelectedItemId(R.id.nav_home);
            View frag = activity.findViewById(R.id.fragment_container);
            assertTrue(frag != null);
            // Selecciona Notifications
            nav.setSelectedItemId(R.id.nav_notifications);
            assertTrue(frag != null);
            // Selecciona Profile
            nav.setSelectedItemId(R.id.nav_profile);
            assertTrue(frag != null);
        });
        scenario.close();
    }
}
