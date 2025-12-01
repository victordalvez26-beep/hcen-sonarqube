package uy.edu.tse.hcen;

import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.ProfileFragment;

@RunWith(AndroidJUnit4.class)
public class ProfileFragmentInstrumentedTest {

    @Test
    public void profileFragmentButtonsAndToggle() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            ProfileFragment f = new ProfileFragment();
            activity.setFragment(f);

            View sectionHelp = activity.findViewById(R.id.sectionHelp);
            View sectionSettings = activity.findViewById(R.id.sectionSettings);
            View sectionHCEN = activity.findViewById(R.id.sectionHCEN);
            assertNotNull(sectionHelp);
            assertNotNull(sectionSettings);
            assertNotNull(sectionHCEN);

            // Toggle sectionHelp
            View header = sectionHelp.findViewById(R.id.headerLayout);
            View content = sectionHelp.findViewById(R.id.contentLayout);
            assertNotNull(header);
            assertNotNull(content);
            header.performClick();
            header.performClick();

            // Click first button in sectionSettings
            Button btn = (Button) ((android.widget.LinearLayout)content).getChildAt(0);
            btn.performClick();
        });
    }

    @Test
    public void profileFragmentLogoutBranch() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            ProfileFragment f = new ProfileFragment();
            activity.setFragment(f);
            Button btnLogout = activity.findViewById(R.id.btnLogout);
            assertNotNull(btnLogout);
            btnLogout.performClick();
        });
    }
}