package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.HelpFragment;

@RunWith(AndroidJUnit4.class)
public class HelpFragmentInstrumentedTest {
    @Test
    public void faqItemExpandsAndCollapses() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            HelpFragment fragment = new HelpFragment();
            activity.setFragment(fragment);
            View faqItem = fragment.getView().findViewById(R.id.containerFaq);
            assertNotNull(faqItem);
            // El primer hijo es el título, el segundo es el primer FAQ real
            View firstFaq = ((android.view.ViewGroup)faqItem).getChildAt(1);
            assertNotNull(firstFaq);
            TextView txtAnswer = firstFaq.findViewById(R.id.txtAnswer);
            ImageView imgArrow = firstFaq.findViewById(R.id.imgArrow);
            // Inicialmente oculto
            assertEquals(View.GONE, txtAnswer.getVisibility());
            // Expande
            firstFaq.findViewById(R.id.headerLayout).performClick();
            assertEquals(View.VISIBLE, txtAnswer.getVisibility());
            // Colapsa
            firstFaq.findViewById(R.id.headerLayout).performClick();
            assertEquals(View.GONE, txtAnswer.getVisibility());
        });
    }
}
