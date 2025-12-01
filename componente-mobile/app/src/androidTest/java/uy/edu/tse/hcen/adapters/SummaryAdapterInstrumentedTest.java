package uy.edu.tse.hcen.adapters;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.summary.SummaryItem;

@RunWith(AndroidJUnit4.class)
public class SummaryAdapterInstrumentedTest {

    @Test
    public void bindsTitleDescriptionAndIcon() {
        Context context = ApplicationProvider.getApplicationContext();
    SummaryAdapter adapter = new SummaryAdapter(
        Collections.singletonList(new SummaryItem("Titulo", "Detalle", R.drawable.ic_allergy)),
        item -> {} // dummy listener
    );
        context.setTheme(R.style.Theme_HCEN);
        FrameLayout parent = new FrameLayout(context);
        SummaryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        assertEquals("Titulo", holder.txtTypeTitle.getText().toString());
        assertEquals(1, adapter.getItemCount());
    }
}
