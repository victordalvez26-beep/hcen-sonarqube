package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import uy.edu.tse.hcen.config.AuthUtils;

@RunWith(AndroidJUnit4.class)
public class AuthUtilsInstrumentedTest {

    @Test
    public void randomStringUsesAllowedAlphabet() {
        String result = AuthUtils.randomString(32);
        assertEquals(32, result.length());
        assertTrue(result.matches("[a-zA-Z0-9]+"));
    }

    @Test
    public void randomStringProducesNonDeterministicValues() {
        String first = AuthUtils.randomString(16);
        String second = AuthUtils.randomString(16);
        assertEquals(16, first.length());
        assertEquals(16, second.length());
        assertNotEquals("Two consecutive random strings should differ", first, second);
    }
}


