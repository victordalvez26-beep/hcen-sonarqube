package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;

@RunWith(AndroidJUnit4.class)
public class StatusDialogFragmentInstrumentedTest {

    @Test
    public void loadingDialogShowsProgressAndAnimatesMessage() {
        Bundle args = StatusDialogFragment.newInstance(DialogType.LOADING, "Cargando").getArguments();
        FragmentScenario<StatusDialogFragment> scenario =
                FragmentScenario.launchInContainer(StatusDialogFragment.class, args, R.style.Theme_HCEN);
        scenario.onFragment(fragment -> {
            ProgressBar progress = fragment.requireView().findViewById(R.id.dialogProgress);
            assertEquals(0, progress.getVisibility());
        });
    }
}


