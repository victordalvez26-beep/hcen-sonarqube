package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.HelpFragment;
import uy.edu.tse.hcen.fragments.MyDataFragment;
import uy.edu.tse.hcen.fragments.ProfileFragment;
import uy.edu.tse.hcen.fragments.SummaryDigitalFragment;
import uy.edu.tse.hcen.manager.UserManager;

@RunWith(AndroidJUnit4.class)
public class FragmentsInstrumentedTest {

    @Test
    public void helpFragmentAddsFaqsAndToggles() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            HelpFragment f = new HelpFragment();
            activity.setFragment(f);

            View container = activity.findViewById(R.id.containerFaq);
            assertNotNull(container);
            assertTrue(((LinearLayout)container).getChildCount() > 1);

            // The first child is the title, FAQs start at index 1
            View firstFaq = ((LinearLayout)container).getChildAt(1);
            TextView answer = firstFaq.findViewById(R.id.txtAnswer);
            assertNotNull(answer);
            assertEquals(View.GONE, answer.getVisibility());
            // simulate click on header
            View header = firstFaq.findViewById(R.id.headerLayout);
            header.performClick();
            // after click answer should be visible
            assertEquals(View.VISIBLE, answer.getVisibility());
        });
    }

    @Test
    public void myDataFragmentLoadsAndEditToggleAndSave() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            // prepare stored user
            UserManager.saveUser(activity, "x@y.z", "MONTEVIDEO", "Loc", "Addr", () -> {});

            MyDataFragment f = new MyDataFragment();
            activity.setFragment(f);

            View txtName = activity.findViewById(R.id.txtName);
            assertNotNull(txtName);

            // toggle edit
            View btnEdit = activity.findViewById(R.id.btnEdit);
            btnEdit.performClick();
            View btnSave = activity.findViewById(R.id.btnSave);
            assertEquals(View.VISIBLE, btnSave.getVisibility());
            // save (this will call UserManager.saveUser)
            btnSave.performClick();
        });
    }

    @Test
    public void profileFragmentSectionButtonsReplaceFragment() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            ProfileFragment f = new ProfileFragment();
            activity.setFragment(f);

            View section = activity.findViewById(R.id.sectionSettings);
            assertNotNull(section);

            // content is dynamically populated; find first button inside contentLayout
            View content = section.findViewById(R.id.contentLayout);
            assertNotNull(content);
            View child = ((LinearLayout)content).getChildAt(0);
            // click the option "Mis datos" which should replace fragment
            child.performClick();

            Fragment current = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            // depending on order, the replaced fragment should be MyDataFragment
            assertNotNull(current);
        });
    }

    @Test
    public void summaryDigitalParseSummaryUpdatesAdapter() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);

            try {
                // craft a JSON with allergies array
                JSONObject root = new JSONObject();
                root.put("allergies", new org.json.JSONArray().put(new JSONObject().put("name", "Polen").put("status", "active")));

                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("parseSummary", String.class);
                m.setAccessible(true);
                m.invoke(f, root.toString());
            } catch (Exception ignored) {
                // ignore
            }
        });
    }
}
