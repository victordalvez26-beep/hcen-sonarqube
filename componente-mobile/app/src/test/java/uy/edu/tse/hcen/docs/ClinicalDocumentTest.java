package uy.edu.tse.hcen.docs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClinicalDocumentTest {

    @Test
    public void fieldsCanBeAssigned() {
        ClinicalDocument doc = new ClinicalDocument();
        doc.id = 1;
        doc.documentFormat = "PDF";
        doc.documentType = "Diagnóstico";
        doc.creationDate = "2024-11-01";
        doc.originClinic = "HCEN";
        doc.professional = "Dr. Smith";
        doc.description = "Detalle";
        doc.accessAllowed = true;
        doc.documentUri = "https://example.com/doc.pdf";

        assertEquals(1, doc.id);
        assertEquals("PDF", doc.documentFormat);
        assertEquals("https://example.com/doc.pdf", doc.documentUri);
    }
}


