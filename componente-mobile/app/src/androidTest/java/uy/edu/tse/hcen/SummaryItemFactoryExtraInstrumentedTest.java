package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import uy.edu.tse.hcen.summary.SummaryItem;
import uy.edu.tse.hcen.summary.SummaryItemFactory;

@RunWith(AndroidJUnit4.class)
public class SummaryItemFactoryExtraInstrumentedTest {

    @Test
    public void conditionsAndImmunizationsAndProceduresAndObservations() throws Exception {
        JSONObject cond = new JSONObject().put("name", "Hipertension").put("status", "active");
        SummaryItem c = SummaryItemFactory.fromCategory("conditions", cond);
        assertEquals("Diagnóstico", c.title);

        JSONObject imm = new JSONObject().put("name", "BCG").put("date", "2020-01-01");
        SummaryItem i = SummaryItemFactory.fromCategory("immunizations", imm);
        assertEquals("Vacuna", i.title);

        JSONObject proc = new JSONObject().put("name", "Apendicectomía").put("date", "2018-02-02");
        SummaryItem p = SummaryItemFactory.fromCategory("procedures", proc);
        assertEquals("Procedimiento", p.title);

        JSONObject obs = new JSONObject().put("type", "Tensión arterial").put("value", "120/80").put("date", "2023-03-03");
        SummaryItem o = SummaryItemFactory.fromCategory("observations", obs);
        assertEquals("Observación", o.title);
        // description contains type and value
        assertEquals("Tensión arterial: 120/80 (2023-03-03)", o.description);
    }
}
