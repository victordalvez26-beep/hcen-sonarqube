package uy.edu.tse.hcen;

import static org.junit.Assert.assertNotNull;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.HomeFragment;

@RunWith(AndroidJUnit4.class)
public class SingleFragmentActivityCoverageTest {

    @Test
    public void testOnCreateAndSetFragment() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            // onCreate se ejecuta automáticamente
            View container = activity.findViewById(R.id.fragment_container);
            assertNotNull(container);
            // setFragment
            Fragment fragment = new HomeFragment();
            activity.setFragment(fragment);
            Fragment current = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            assertNotNull(current);
            assertNotNull(current.getView());
        });
        scenario.close();
    }
}
