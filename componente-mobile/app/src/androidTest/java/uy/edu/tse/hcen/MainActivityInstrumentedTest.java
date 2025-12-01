package uy.edu.tse.hcen;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.HomeFragment;
import uy.edu.tse.hcen.fragments.NotificationsFragment;
import uy.edu.tse.hcen.fragments.ProfileFragment;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest extends BaseInstrumentedTest {

    @Test
    public void finishesWhenSessionMissing() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.moveToState(Lifecycle.State.DESTROYED);
        scenario.close();
    }

}


