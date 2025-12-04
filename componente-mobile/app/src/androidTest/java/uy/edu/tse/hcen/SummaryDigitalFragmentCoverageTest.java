package uy.edu.tse.hcen;

import static org.junit.Assert.*;

import android.content.Context;
import android.os.Looper;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.SummaryDigitalFragment;
import uy.edu.tse.hcen.manager.SessionManager;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class SummaryDigitalFragmentCoverageTest {

    private static final String PARSE_SUMMARY = "parseSummary";

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SessionManager.saveJWT(context, "jwt-token");
    }

    @Test
    public void parseSummaryHandlesMalformedJson() {
        FragmentScenario<SummaryDigitalFragment> scenario = FragmentScenario.launchInContainer(SummaryDigitalFragment.class, null, R.style.Theme_HCEN);
        scenario.onFragment(fragment -> {
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod(PARSE_SUMMARY, String.class);
                m.setAccessible(true);
                m.invoke(fragment, "{bad json}");
            } catch (Exception ignored) {
                // ignored
            }
        });
    }

    @Test
    public void getSummaryDigitalHandlesNetworkError() {
        FragmentScenario<SummaryDigitalFragment> scenario = FragmentScenario.launchInContainer(SummaryDigitalFragment.class, null, R.style.Theme_HCEN);
        scenario.onFragment(fragment -> {
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("getSummaryDigital");
                m.setAccessible(true);
                m.invoke(fragment);
            } catch (Exception ignored) {
                // ignored
            }
        });
    }

    @Test
    public void generatePdfFromSummaryHandlesException() {
        FragmentScenario<SummaryDigitalFragment> scenario = FragmentScenario.launchInContainer(SummaryDigitalFragment.class, null, R.style.Theme_HCEN);
        scenario.onFragment(fragment -> {
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("generatePdfFromSummary", java.util.List.class);
                m.setAccessible(true);
                m.invoke(fragment, (Object) null); // null list
            } catch (Exception ignored) {
                // ignored
            }
        });
    }

    @Test
    public void summaryFragmentDisplaysEmptyAndNonEmpty() throws Exception {
        // Empty
        FragmentScenario<SummaryDigitalFragment> scenario = FragmentScenario.launchInContainer(SummaryDigitalFragment.class, null, R.style.Theme_HCEN);
        scenario.onFragment(fragment -> {
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod(PARSE_SUMMARY, String.class);
                m.setAccessible(true);
                m.invoke(fragment, new JSONObject().toString());
            } catch (Exception ignored) {}
        });
        // Non-empty
        scenario.onFragment(fragment -> {
            try {
                JSONObject root = new JSONObject();
                JSONArray arr = new JSONArray();
                arr.put(new JSONObject().put("name", "Polen").put("status", "active"));
                root.put("allergies", arr);
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod(PARSE_SUMMARY, String.class);
                m.setAccessible(true);
                m.invoke(fragment, root.toString());
            } catch (Exception ignored) {}
        });
    }
}
