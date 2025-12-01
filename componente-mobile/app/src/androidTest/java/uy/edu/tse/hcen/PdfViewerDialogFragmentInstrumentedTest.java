package uy.edu.tse.hcen;

import static org.junit.Assert.assertSame;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.fragments.PdfViewerDialogFragment;

@RunWith(AndroidJUnit4.class)
public class PdfViewerDialogFragmentInstrumentedTest {

    @Test
    public void displaysProvidedBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        FragmentScenario<PdfViewerDialogFragment> scenario =
                FragmentScenario.launchInContainer(PdfViewerDialogFragment.class,
                        PdfViewerDialogFragment.newInstance(bitmap).getArguments(),
                        R.style.Theme_HCEN);

        scenario.onFragment(fragment -> {
            android.widget.ImageView view = fragment.requireView().findViewById(R.id.imagePdf);
            BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
            assertSame(bitmap, drawable.getBitmap());
        });
    }
}


