package uy.edu.tse.hcen.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import uy.edu.tse.hcen.docs.ClinicalDocument;

@RunWith(AndroidJUnit4.class)
public class ClinicalDocumentsAdapterInstrumentedTest {

    @Test
    public void bindsDocumentAndHandlesClick() {
        Context context = ApplicationProvider.getApplicationContext();
        ClinicalDocument doc = new ClinicalDocument();
        doc.documentType = "Diagnóstico";
        doc.creationDate = "2024-10-01";
        doc.originClinic = "HCEN";
        doc.professional = "Dr. Smith";
        doc.description = "Detalle";

        final boolean[] clicked = {false};
        ClinicalDocumentsAdapter adapter = new ClinicalDocumentsAdapter(
                Collections.singletonList(doc),
                d -> clicked[0] = d == doc
        );

        FrameLayout parent = new FrameLayout(context);
        ClinicalDocumentsAdapter.DocViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        assertEquals("Diagnóstico", holder.type.getText().toString());
        holder.card.performClick();
        assertTrue(clicked[0]);
    }
}


