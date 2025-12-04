package uy.edu.tse.hcen;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import uy.edu.tse.hcen.model.Department;
import uy.edu.tse.hcen.model.User;

@RunWith(AndroidJUnit4.class)
public class DepartmentAndUserInstrumentedTest {

    @Test
    public void departmentSpecialNamesAreFormatted() {
        assertEquals("Cerro Largo", Department.CERRO_LARGO.getDisplayName());
        assertEquals("Río Negro", Department.RIO_NEGRO.getDisplayName());
        assertEquals("San José", Department.SAN_JOSE.getDisplayName());
    }

    @Test
    public void departmentDefaultFormattingCapitalizesName() {
        assertEquals("Artigas", Department.ARTIGAS.getDisplayName());
        assertEquals("Lavalleja", Department.LAVALLEJA.getDisplayName());
    }

    @Test
    public void userFullNameSkipsNullsAndBlanks() {
        User user = new User(
                "user@mail.com",
                "Ana",
                "",
                "Suarez",
                null,
                "CI",
                "12345678",
                "UY",
                null,
                Department.ARTIGAS,
                "Paysandú",
                "Calle 123"
        );

        assertEquals("Ana Suarez", user.getFullName());
    }

    @Test
    public void userFullNameConcatenatesAllParts() {
        User user = new User(
                "user@mail.com",
                "Juan",
                "Carlos",
                "Pérez",
                "García",
                "CI",
                "9876543",
                "UY",
                null,
                Department.MONTEVIDEO,
                "Montevideo",
                "Av. Italia 1234"
        );

        assertEquals("Juan Carlos Pérez García", user.getFullName());
    }
}


