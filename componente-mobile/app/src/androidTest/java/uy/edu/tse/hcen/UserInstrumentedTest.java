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
    @Test
    public void userConstructorAndGetters() {
        Date birth = new Date(1234567890L);
        User user = new User("mail", "Juan", "Carlos", "Perez", "Gomez", "DNI", "1234", "UY", birth, Department.Montevideo, "Loc", "Addr");
        assertEquals("mail", user.getEmail());
        assertEquals("Juan", user.getFirstName());
        assertEquals("Carlos", user.getSecondName());
        assertEquals("Perez", user.getFirstLastName());
        assertEquals("Gomez", user.getSecondLastName());
        assertEquals("DNI", user.getDocumentType());
        assertEquals("1234", user.getDocumentCode());
        assertEquals("UY", user.getNationality());
        assertEquals(birth, user.getBirthdate());
    assertEquals(Department.Montevideo, user.getDepartamento());
        assertEquals("Loc", user.getLocation());
        assertEquals("Addr", user.getAddress());
    }

    @Test
    public void getFullNameAllBranches() {
    User user = new User("mail", "Juan", "Carlos", "Perez", "Gomez", "DNI", "1234", "UY", null, Department.Montevideo, null, null);
        assertEquals("Juan Carlos Perez Gomez", user.getFullName());

    user = new User("mail", "Juan", "", "Perez", "", "DNI", "1234", "UY", null, Department.Montevideo, null, null);
        assertEquals("Juan Perez", user.getFullName());

    user = new User("mail", null, null, null, null, "DNI", "1234", "UY", null, Department.Montevideo, null, null);
        assertEquals("", user.getFullName());
    }
}