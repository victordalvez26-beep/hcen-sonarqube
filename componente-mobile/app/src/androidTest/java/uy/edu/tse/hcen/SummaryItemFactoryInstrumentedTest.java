package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import uy.edu.tse.hcen.summary.SummaryItem;
import uy.edu.tse.hcen.summary.SummaryItemFactory;

@RunWith(AndroidJUnit4.class)
public class SummaryItemFactoryInstrumentedTest {
    @Test
    public void allergiesCategoryDefaultsAndMissingFields() throws Exception {
        // Sin campos
        JSONObject payload = new JSONObject();
        SummaryItem item = SummaryItemFactory.fromCategory("allergies", payload);
        assertEquals("Alergia", item.title);
        assertEquals("Alergia ()", item.description);
        assertEquals(R.drawable.ic_allergy, item.iconResId);
    }

    @Test
    public void medicationsCategoryMissingDose() throws Exception {
        JSONObject payload = new JSONObject().put("name", "Ibuprofeno");
        SummaryItem item = SummaryItemFactory.fromCategory("medications", payload);
        assertEquals("Medicación", item.title);
        assertEquals("Ibuprofeno - ", item.description);
        assertEquals(R.drawable.ic_medication, item.iconResId);
    }

    @Test
    public void extractDescriptionHandlesJSONException() {
        // Simula un JSONObject que lanza excepción
        SummaryItem item = SummaryItemFactory.fromCategory("allergies", new org.json.JSONObject() {
            @Override
            public String optString(String name, String fallback) {
                throw new RuntimeException("fail");
            }
        });
        assertEquals("Alergia", item.title);
        assertEquals("Información no disponible", item.description);
        assertEquals(R.drawable.ic_allergy, item.iconResId);
    }

    @Test
    public void unknownCategoryWithFields() throws Exception {
        JSONObject payload = new JSONObject().put("name", "Test");
        SummaryItem item = SummaryItemFactory.fromCategory("unknown", payload);
        assertEquals("unknown", item.title);
        assertEquals("Información no disponible", item.description);
        assertEquals(R.drawable.ic_fhir_generic, item.iconResId);
    }

    @Test
    public void allergiesCategoryUsesExpectedTitleIconAndDescription() throws Exception {
        JSONObject payload = new JSONObject()
                .put("name", "Polen")
                .put("status", "active");

        SummaryItem item = SummaryItemFactory.fromCategory("allergies", payload);

        assertEquals("Alergia", item.title);
        assertEquals("Polen (active)", item.description);
        assertEquals(R.drawable.ic_allergy, item.iconResId);
    }

    @Test
    public void medicationsCategoryIncludesDoseInDescription() throws Exception {
        JSONObject payload = new JSONObject()
                .put("name", "Ibuprofeno")
                .put("dose", "200mg cada 8h");

        SummaryItem item = SummaryItemFactory.fromCategory("medications", payload);

        assertEquals("Medicación", item.title);
        assertEquals("Ibuprofeno - 200mg cada 8h", item.description);
        assertEquals(R.drawable.ic_medication, item.iconResId);
    }

    @Test
    public void unknownCategoryFallsBackToGenericResources() {
        SummaryItem item = SummaryItemFactory.fromCategory("labs", new JSONObject());

        assertEquals("labs", item.title);
        assertEquals("Información no disponible", item.description);
        assertEquals(R.drawable.ic_fhir_generic, item.iconResId);
    }
}


