package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para DocumentoServiceBean.
 * Esta clase es un bean EJB vacío, solo verificamos que se puede instanciar.
 */
class DocumentoServiceBeanTest {

    @Test
    void instantiate_shouldSucceed() {
        DocumentoServiceBean bean = new DocumentoServiceBean();
        assertNotNull(bean);
    }

    @Test
    void bean_shouldBeInstanceOfDocumentoService() {
        DocumentoServiceBean bean = new DocumentoServiceBean();
        assertTrue(bean instanceof DocumentoService);
    }
}

