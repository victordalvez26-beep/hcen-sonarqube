package uy.edu.tse.hcen;

import static org.junit.Assert.assertNotNull;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.SingleFragmentActivity;
import uy.edu.tse.hcen.fragments.HelpFragment;

@RunWith(AndroidJUnit4.class)
public class SingleFragmentActivityInstrumentedTest {

    @Test
    public void setFragmentReplacesFragment() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            HelpFragment fragment = new HelpFragment();
            activity.setFragment(fragment);
            Fragment current = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            assertNotNull(current);
        });
    }
}