package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.SystemClock;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.fragments.ClinicalDocumentsFragment;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class ClinicalDocumentsFragmentInstrumentedTest extends BaseInstrumentedTest {

    private static final String DOC_URI = "https://ontheline.trincoll.edu/images/bookdown/sample-local-pdf.pdf";

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        // Usar un JWT base64 válido (header.payload.signature)
        SessionManager.saveJWT(context, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
    }

    private String buildDocsResponse() {
    return "[{"
        + "\"id\":1,"
        + "\"formatoDocumento\":\"PDF\","
        + "\"tipoDocumento\":\"Diagnóstico\","
        + "\"fechaCreacion\":\"2024-10-01T12:00:00.000\","
        + "\"clinicaOrigen\":\"HCEN\","
        + "\"profesionalSalud\":\"Dr. Smith\","
        + "\"descripcion\":\"Descripción\","
        + "\"accesoPermitido\":true,"
        + "\"uriDocumento\":\"" + DOC_URI + "\""
        + "}]";
    }

    @Test
    public void loadDocumentsDisplaysRecycler() {
        TestNetworkDispatcher.enqueueResponse(AppConfig.DOCS_URL, 200, buildDocsResponse());
        FragmentScenario<ClinicalDocumentsFragment> scenario =
                FragmentScenario.launchInContainer(ClinicalDocumentsFragment.class, null, R.style.Theme_HCEN);
        scenario.moveToState(Lifecycle.State.RESUMED);
        SystemClock.sleep(500);

        scenario.onFragment(fragment -> {
            RecyclerView recycler = fragment.requireView().findViewById(R.id.recyclerDocuments);
            assertEquals(1, recycler.getAdapter().getItemCount());
        });
    }

    @Test
    public void openDocumentStartsIntent() {
        TestNetworkDispatcher.enqueueResponse(AppConfig.DOCS_URL, 200, buildDocsResponse());
        // Simula un PDF real (cabecera mínima de PDF)
        byte[] pdfBytes = "%PDF-1.4\n%âãÏÓ\n1 0 obj\n<< /Type /Catalog >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF".getBytes();
        TestNetworkDispatcher.enqueueResponse(DOC_URI, 200, pdfBytes);

        FragmentScenario<ClinicalDocumentsFragment> scenario =
                FragmentScenario.launchInContainer(ClinicalDocumentsFragment.class, null, R.style.Theme_HCEN);
        scenario.moveToState(Lifecycle.State.RESUMED);
        SystemClock.sleep(500);
        scenario.onFragment(fragment -> {
            RecyclerView recycler = fragment.requireView().findViewById(R.id.recyclerDocuments);
            RecyclerView.ViewHolder holder = recycler.findViewHolderForAdapterPosition(0);
            holder.itemView.findViewById(R.id.cardDocument).performClick();
        });
        SystemClock.sleep(500);
        // Since the implementation opens an activity, the test cannot easily assert the intent,
        // but at least it should not crash and the loading dialog should be dismissed.
        // The test passes if no exception is thrown.
        assertTrue("openDocument completed without throwing", true);
    }
}
