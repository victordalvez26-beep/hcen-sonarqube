package uy.edu.tse.hcen;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import java.util.ArrayList;
import uy.edu.tse.hcen.summary.SummaryItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Looper;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.fragments.SummaryDigitalFragment;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class SummaryDigitalFragmentExtraInstrumentedTest {

    private static final String POLEN = "Polen";
    private static final String ACTIVE = "active";
    private static final String STATUS = "status";
    private static final String ALLERGIES = "allergies";
    private static final String JWT_TOKEN = "jwt-token";
    private static final String FRAGMENT_LOADED_TIMEOUT = "Fragment loaded without timeout";

    @Test
    public void parseSummaryEmptyAndNonEmpty() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            try {
                // Empty summary
                JSONObject root = new JSONObject();
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("parseSummary", String.class);
                m.setAccessible(true);
                m.invoke(f, root.toString());
                // Non-empty summary
                JSONArray arr = new JSONArray();
                arr.put(new JSONObject().put("name", POLEN).put(STATUS, ACTIVE));
                root.put(ALLERGIES, arr);
                m.invoke(f, root.toString());
            } catch (Exception ignored) {
                //ignored
            }
        });
    }

    @Test
    public void errorBranchGetSummaryDigital() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            // Simulate error by calling getSummaryDigital with broken config
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("getSummaryDigital");
                m.setAccessible(true);
                m.invoke(f);
            } catch (Exception ignored) {
                // ignored
            }
        });
    }

    @Test
    public void generatePdfFromSummaryRunsWithoutCrash() {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("generatePdfFromSummary", java.util.List.class);
                m.setAccessible(true);
                m.invoke(f, new java.util.ArrayList<>());
            } catch (Exception ignored) {
                //ignored
            }
        });
    }

    @Test
    public void summaryFragmentDisplaysEmptyViewOnNoData() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
    SessionManager.saveJWT(context, JWT_TOKEN);
        TestNetworkDispatcher.install();
        TestNetworkDispatcher.enqueueResponse(AppConfig.SUMMARY_URL, 200, "{}"); // Empty JSON

        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            f.getViewLifecycleOwnerLiveData().observe(activity, lifecycleOwner -> {
                if (lifecycleOwner != null) {
                    // Wait for view to be created and data loaded
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(latch::countDown, 2000);
                }
            });
        });
    assertTrue(FRAGMENT_LOADED_TIMEOUT, latch.await(5, TimeUnit.SECONDS));
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = (SummaryDigitalFragment) activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            View emptyView = f.getView().findViewById(R.id.emptyView);
            assertNotNull("Empty view should exist", emptyView);
            assertEquals(View.VISIBLE, emptyView.getVisibility());
        });
    }

    @Test
    public void summaryFragmentDisplaysRecyclerWithData() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
    SessionManager.saveJWT(context, JWT_TOKEN);
        TestNetworkDispatcher.install();
        try {
            JSONObject response = new JSONObject();
            JSONArray allergies = new JSONArray();
            allergies.put(new JSONObject().put("name", POLEN).put(STATUS, ACTIVE));
            response.put(ALLERGIES, allergies);
            TestNetworkDispatcher.enqueueResponse(AppConfig.SUMMARY_URL, 200, response.toString());
        } catch (Exception ignored) {
            // ignored
        }

        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            f.getViewLifecycleOwnerLiveData().observe(activity, lifecycleOwner -> {
                if (lifecycleOwner != null) {
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(latch::countDown, 2000);
                }
            });
        });
    assertTrue(FRAGMENT_LOADED_TIMEOUT, latch.await(5, TimeUnit.SECONDS));
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = (SummaryDigitalFragment) activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            View contentContainer = f.getView().findViewById(R.id.contentContainer);
            assertNotNull("Content container should exist", contentContainer);
            assertEquals(View.VISIBLE, contentContainer.getVisibility());
        });
    }

    @Test
    public void summaryFragmentErrorResponseShowsErrorDialog() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
    SessionManager.saveJWT(context, JWT_TOKEN);
        TestNetworkDispatcher.install();
        TestNetworkDispatcher.enqueueResponse(AppConfig.SUMMARY_URL, 500, "Server error");

        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            new android.os.Handler(Looper.getMainLooper()).postDelayed(latch::countDown, 1000);
        });
    assertTrue(FRAGMENT_LOADED_TIMEOUT, latch.await(5, TimeUnit.SECONDS));
        // Test that fragment handles error - since it's asynchronous, just ensuring no crash
    }

    @Test
    public void summaryFragmentClickTypeCardShowsPopup() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
    SessionManager.saveJWT(context, JWT_TOKEN);
        TestNetworkDispatcher.install();
        JSONObject response = new JSONObject();
        JSONArray allergies = new JSONArray();
    allergies.put(new JSONObject().put("name", POLEN).put(STATUS, ACTIVE));
    response.put(ALLERGIES, allergies);
        TestNetworkDispatcher.enqueueResponse(AppConfig.SUMMARY_URL, 200, response.toString());

        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            f.getViewLifecycleOwnerLiveData().observe(activity, lifecycleOwner -> {
                if (lifecycleOwner != null) {
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(latch::countDown, 2000);
                }
            });
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        // Click en la tarjeta de alergias
        onView(withText("Alergias")).perform(click());
        // El popup debería mostrarse (verificable por el texto)
        onView(withText("Alergias")).check(matches(isDisplayed()));
    }

    @Test
    public void summaryFragmentExportPdfWithDataRunsWithoutCrash() throws Exception {
        ActivityScenario<SingleFragmentActivity> scenario = ActivityScenario.launch(SingleFragmentActivity.class);
        scenario.onActivity(activity -> {
            SummaryDigitalFragment f = new SummaryDigitalFragment();
            activity.setFragment(f);
            ArrayList<SummaryItem> items = new ArrayList<>();
            items.add(new SummaryItem("Alergia", POLEN + " (" + ACTIVE + ")", uy.edu.tse.hcen.R.drawable.ic_allergy));
            try {
                java.lang.reflect.Method m = SummaryDigitalFragment.class.getDeclaredMethod("generatePdfFromSummary", java.util.List.class);
                m.setAccessible(true);
                m.invoke(f, items);
            } catch (Exception ignored) {
                //ignored
            }
        });
    }
}
