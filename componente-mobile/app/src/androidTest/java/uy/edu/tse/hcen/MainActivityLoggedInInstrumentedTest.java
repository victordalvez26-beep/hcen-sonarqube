package uy.edu.tse.hcen;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class MainActivityLoggedInInstrumentedTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SessionManager.saveJWT(context, "jwt-test");
    }

    @Test
    public void launchesWhenLoggedInAndBottomNavWorks() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> {
            BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
            assertTrue(nav.getMenu().size() > 0);
            // select profile
            nav.setSelectedItemId(R.id.nav_profile);
            View frag = activity.findViewById(R.id.fragment_container);
            assertTrue(frag != null);
        });
        scenario.close();
    }
}
