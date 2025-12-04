package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.Department;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class UserInstrumentedTest {

    private static final String SECOND_NAME = "Carlos";
    private static final String SECOND_LAST_NAME = "Gomez";
    private static final String FIRST_LAST_NAME = "Perez";

    @Test
    public void userConstructorAndGetters() {
        Date birth = new Date(1234567890L);
        User user = new User("mail", "Juan", SECOND_NAME, FIRST_LAST_NAME, SECOND_LAST_NAME, "DNI", "1234", "UY", birth, Department.MONTEVIDEO, "Loc", "Addr");
        assertEquals("mail", user.getEmail());
        assertEquals("Juan", user.getFirstName());
        assertEquals(SECOND_NAME, user.getSecondName());
        assertEquals(FIRST_LAST_NAME, user.getFirstLastName());
        assertEquals(SECOND_LAST_NAME, user.getSecondLastName());
        assertEquals("DNI", user.getDocumentType());
        assertEquals("1234", user.getDocumentCode());
        assertEquals("UY", user.getNationality());
        assertEquals(birth, user.getBirthdate());
        assertEquals(Department.MONTEVIDEO, user.getDepartamento());
        assertEquals("Loc", user.getLocation());
        assertEquals("Addr", user.getAddress());
    }

    @Test
    public void getFullNameAllBranches() {
        User user = new User("mail", "Juan", SECOND_NAME, FIRST_LAST_NAME, SECOND_LAST_NAME, "DNI", "1234", "UY", null, Department.MONTEVIDEO, null, null);
        assertEquals("Juan Carlos Perez Gomez", user.getFullName());

        user = new User("mail", "Juan", "", FIRST_LAST_NAME, "", "DNI", "1234", "UY", null, Department.MONTEVIDEO, null, null);
        assertEquals("Juan Perez", user.getFullName());

        user = new User("mail", null, null, null, null, "DNI", "1234", "UY", null, Department.MONTEVIDEO, null, null);
        assertEquals("", user.getFullName());
    }
}