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
    private static final String CATEGORY_ALLERGIES = "allergies";
    private static final String CATEGORY_MEDICATIONS = "medications";
    private static final String CATEGORY_UNKNOWN = "unknown";
    private static final String CATEGORY_LABS = "labs";
    private static final String TITLE_ALERGIA = "Alergia";
    private static final String TITLE_MEDICACION = "Medicación";
    private static final String DESCRIPTION_ALERGIA = "Alergia ()";
    private static final String DESCRIPTION_INFO_NOT_AVAILABLE = "Información no disponible";
    private static final String DESCRIPTION_IBUPROFENO_EMPTY = "Ibuprofeno - ";
    private static final String DESCRIPTION_IBUPROFENO_DOSE = "Ibuprofeno - 200mg cada 8h";
    private static final String NAME_POLEN = "Polen";
    private static final String STATUS_ACTIVE = "active";
    private static final String NAME_TEST = "Test";
    private static final String CUSTOM_EXCEPTION_MESSAGE = "JSON extraction failed";
    @Test
    public void allergiesCategoryDefaultsAndMissingFields() throws Exception {
        // Sin campos
        JSONObject payload = new JSONObject();
        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_ALLERGIES, payload);
        assertEquals(TITLE_ALERGIA, item.title);
        assertEquals(DESCRIPTION_ALERGIA, item.description);
        assertEquals(R.drawable.ic_allergy, item.iconResId);
    }

    @Test
    public void medicationsCategoryMissingDose() throws Exception {
        JSONObject payload = new JSONObject().put("name", "Ibuprofeno");
        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_MEDICATIONS, payload);
        assertEquals(TITLE_MEDICACION, item.title);
        assertEquals(DESCRIPTION_IBUPROFENO_EMPTY, item.description);
        assertEquals(R.drawable.ic_medication, item.iconResId);
    }

    @Test
    public void extractDescriptionHandlesJSONException() {
        // Simula un JSONObject que lanza excepción
        class CustomJSONException extends RuntimeException {
            public CustomJSONException(String message) {
                super(message);
            }
        }
        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_ALLERGIES, new org.json.JSONObject() {
            @Override
            public String optString(String name, String fallback) {
                throw new CustomJSONException(CUSTOM_EXCEPTION_MESSAGE);
            }
        });
        assertEquals(TITLE_ALERGIA, item.title);
        assertEquals(DESCRIPTION_INFO_NOT_AVAILABLE, item.description);
        assertEquals(R.drawable.ic_allergy, item.iconResId);
    }

    @Test
    public void unknownCategoryWithFields() throws Exception {
        JSONObject payload = new JSONObject().put("name", NAME_TEST);
        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_UNKNOWN, payload);
        assertEquals(CATEGORY_UNKNOWN, item.title);
        assertEquals(DESCRIPTION_INFO_NOT_AVAILABLE, item.description);
        assertEquals(R.drawable.ic_fhir_generic, item.iconResId);
    }

    @Test
    public void allergiesCategoryUsesExpectedTitleIconAndDescription() throws Exception {
        JSONObject payload = new JSONObject()
                .put("name", NAME_POLEN)
                .put("status", STATUS_ACTIVE);

        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_ALLERGIES, payload);

        assertEquals(TITLE_ALERGIA, item.title);
        assertEquals(NAME_POLEN + " (" + STATUS_ACTIVE + ")", item.description);
        assertEquals(R.drawable.ic_allergy, item.iconResId);
    }

    @Test
    public void medicationsCategoryIncludesDoseInDescription() throws Exception {
        JSONObject payload = new JSONObject()
                .put("name", "Ibuprofeno")
                .put("dose", "200mg cada 8h");

        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_MEDICATIONS, payload);

        assertEquals(TITLE_MEDICACION, item.title);
        assertEquals(DESCRIPTION_IBUPROFENO_DOSE, item.description);
        assertEquals(R.drawable.ic_medication, item.iconResId);
    }

    @Test
    public void unknownCategoryFallsBackToGenericResources() {
        SummaryItem item = SummaryItemFactory.fromCategory(CATEGORY_LABS, new JSONObject());

        assertEquals(CATEGORY_LABS, item.title);
        assertEquals(DESCRIPTION_INFO_NOT_AVAILABLE, item.description);
        assertEquals(R.drawable.ic_fhir_generic, item.iconResId);
    }
}


