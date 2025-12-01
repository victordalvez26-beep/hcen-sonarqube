package uy.edu.tse.hcen.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para {@link UserUuidUtil}.
 */
class UserUuidUtilTest {

    @Test
    void generateUuid_nullDocumento_shouldReturnNull() {
        assertNull(UserUuidUtil.generateUuid(null));
    }

    @Test
    void generateUuid_trimsDocumentoAndPrefixesCorrectly() {
        String uuid = UserUuidUtil.generateUuid(" 12345678 ");
        assertEquals("uy-ci-12345678", uuid);
    }

    @Test
    void extractDocumentoFromUid_validFormat_shouldReturnDocumento() {
        String documento = UserUuidUtil.extractDocumentoFromUid("uy-ci-12345678");
        assertEquals("12345678", documento);
    }

    @Test
    void extractDocumentoFromUid_nullOrBlank_shouldReturnNull() {
        assertNull(UserUuidUtil.extractDocumentoFromUid(null));
        assertNull(UserUuidUtil.extractDocumentoFromUid("   "));
    }

    @Test
    void extractDocumentoFromUid_invalidFormat_shouldReturnNull() {
        assertNull(UserUuidUtil.extractDocumentoFromUid("invalid-format"));
        assertNull(UserUuidUtil.extractDocumentoFromUid("uy-ci"));
    }
}


