package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.fragments.NotificationsFragment;
import uy.edu.tse.hcen.notifications.NotificationStorage;

@RunWith(AndroidJUnit4.class)
public class NotificationsFragmentInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotificationStorage.clear(context);
    }

    @Test
    public void showsEmptyViewWhenNoNotifications() {
        FragmentScenario<NotificationsFragment> scenario =
                FragmentScenario.launchInContainer(NotificationsFragment.class, null, R.style.Theme_HCEN);

        scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onFragment(fragment -> {
            View empty = fragment.requireView().findViewById(R.id.emptyView);
            RecyclerView recycler = fragment.requireView().findViewById(R.id.recyclerNotifications);
            assertEquals(View.VISIBLE, empty.getVisibility());
            assertEquals(View.GONE, recycler.getVisibility());
        });
    }

    @Test
    public void showsRecyclerContentWhenNotificationsExist() {
        NotificationStorage.saveNotification(context, "Mensaje 1");
        NotificationStorage.saveNotification(context, "Mensaje 2");

        FragmentScenario<NotificationsFragment> scenario =
                FragmentScenario.launchInContainer(NotificationsFragment.class, null, R.style.Theme_HCEN);

        scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onFragment(fragment -> {
            View empty = fragment.requireView().findViewById(R.id.emptyView);
            RecyclerView recycler = fragment.requireView().findViewById(R.id.recyclerNotifications);
            assertEquals(View.GONE, empty.getVisibility());
            assertEquals(View.VISIBLE, recycler.getVisibility());
            assertTrue(recycler.getAdapter().getItemCount() >= 2);
        });
    }
}


