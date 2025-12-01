package uy.gub.agesic.inus.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uy.gub.agesic.inus.model.InusUsuario;
import uy.gub.agesic.inus.model.UsuarioPrestadorAssociation;
import uy.gub.agesic.inus.service.InusService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InusControllerTest {

    private static final String UID_TEST = "uid-test";
    private static final String SUCCESS = "success";
    private static final String EMAIL = "email";
    private static final String ERROR_MSG = "Error";
    private static final String TOTAL = "total";
    private static final String RUT = "rut";

    @Mock
    private InusService inusService;

    @InjectMocks
    private InusController inusController;

    private InusUsuario testUser;

    @BeforeEach
    void setUp() {
        testUser = new InusUsuario();
        testUser.setUid(UID_TEST);
        testUser.setPrimerNombre("Test");
    }

    @Test
    void obtenerUsuarioShouldReturnUserWhenExists() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuario(UID_TEST);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(SUCCESS));
        assertEquals(testUser, response.getBody().get("usuario"));
    }

    @Test
    void obtenerUsuarioShouldReturnNotFoundWhenNotExists() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuario(UID_TEST);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void obtenerUsuarioPorNombreYFechaNacimientoShouldReturnUserWhenExists() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        when(inusService.findByNombreApellidoAndFechaNacimiento("Test", "User", dob))
                .thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYFechaNacimiento(
                "Test", "User", "2000-01-01");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void obtenerUsuarioPorNombreYFechaNacimientoShouldReturnBadRequestWhenDateInvalid() {
        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYFechaNacimiento(
                "Test", "User", "invalid-date");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void obtenerUsuarioPorNombreYFechaNacimientoShouldReturnNotFoundWhenNotExists() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        when(inusService.findByNombreApellidoAndFechaNacimiento("Test", "User", dob))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYFechaNacimiento(
                "Test", "User", "2000-01-01");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void obtenerUsuarioPorNombreYTelefonoShouldReturnUser() {
        when(inusService.findByNombreApellidoAndTelefono("Test", "User", "123"))
                .thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYTelefono(
                "Test", "User", "123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerUsuarioPorNombreYTelefonoShouldReturnNotFound() {
        when(inusService.findByNombreApellidoAndTelefono("Test", "User", "123"))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYTelefono(
                "Test", "User", "123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void obtenerUsuarioPorNombreYEmailShouldReturnUser() {
        when(inusService.findByNombreApellidoAndEmail("Test", "User", EMAIL))
                .thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYEmail(
                "Test", "User", EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerUsuarioPorNombreYEmailShouldReturnNotFound() {
        when(inusService.findByNombreApellidoAndEmail("Test", "User", EMAIL))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuarioPorNombreYEmail(
                "Test", "User", EMAIL);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void crearUsuarioShouldCreateWhenNotExists() {
        when(inusService.existsByUid(UID_TEST)).thenReturn(false);
        when(inusService.save(any())).thenReturn(testUser);
        when(inusService.findAll()).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.crearUsuario(testUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void crearUsuarioShouldReturnConflictWhenExists() {
        when(inusService.existsByUid(UID_TEST)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = inusController.crearUsuario(testUser);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void crearUsuarioShouldReturnErrorWhenException() {
        when(inusService.existsByUid(UID_TEST)).thenThrow(new RuntimeException(ERROR_MSG));

        ResponseEntity<Map<String, Object>> response = inusController.crearUsuario(testUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void actualizarUsuarioShouldUpdateWhenExists() {
        when(inusService.update(eq(UID_TEST), any())).thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.actualizarUsuario(UID_TEST, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void actualizarUsuarioShouldReturnNotFoundWhenNotExists() {
        when(inusService.update(eq(UID_TEST), any())).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.actualizarUsuario(UID_TEST, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void actualizarUsuarioShouldReturnErrorWhenException() {
        when(inusService.update(eq(UID_TEST), any())).thenThrow(new RuntimeException(ERROR_MSG));

        ResponseEntity<Map<String, Object>> response = inusController.actualizarUsuario(UID_TEST, testUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void eliminarUsuarioShouldDeleteWhenExists() {
        when(inusService.deleteByUid(UID_TEST)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = inusController.eliminarUsuario(UID_TEST);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarUsuarioShouldReturnNotFoundWhenNotExists() {
        when(inusService.deleteByUid(UID_TEST)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = inusController.eliminarUsuario(UID_TEST);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void eliminarUsuarioShouldReturnErrorWhenException() {
        when(inusService.deleteByUid(UID_TEST)).thenThrow(new RuntimeException(ERROR_MSG));

        ResponseEntity<Map<String, Object>> response = inusController.eliminarUsuario(UID_TEST);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void listarUsuariosShouldReturnList() {
        when(inusService.findAll()).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.listarUsuarios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get(TOTAL));
    }

    @Test
    void obtenerUsuariosPorPrestadorIdShouldReturnList() {
        when(inusService.findUsuariosCompletosByPrestadorId(1L)).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuariosPorPrestadorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get(TOTAL));
    }

    @Test
    void obtenerUsuariosPorPrestadorRutShouldReturnList() {
        when(inusService.findUsuariosCompletosByPrestadorRut(RUT)).thenReturn(Collections.singletonList(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.obtenerUsuariosPorPrestadorRut(RUT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get(TOTAL));
    }

    @Test
    void obtenerPrestadoresPorUsuarioShouldReturnListWhenUserExists() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        when(inusService.findPrestadoresByUsuario(UID_TEST)).thenReturn(Collections.singletonList(1L));
        when(inusService.findAllAsociaciones()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = inusController.obtenerPrestadoresPorUsuario(UID_TEST);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(SUCCESS));
    }

    @Test
    void obtenerPrestadoresPorUsuarioShouldReturnNotFoundWhenUserNotExists() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.obtenerPrestadoresPorUsuario(UID_TEST);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void asociarUsuarioConPrestadorShouldAssociateWhenUserExistsAndParamsValid() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        when(inusService.crearAsociacion(anyString(), anyLong(), anyString())).thenReturn(new UsuarioPrestadorAssociation());

        Map<String, Object> request = new HashMap<>();
        request.put("prestadorId", 1);
        request.put("prestadorRut", RUT);

        ResponseEntity<Map<String, Object>> response = inusController.asociarUsuarioConPrestador(UID_TEST, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void asociarUsuarioConPrestadorShouldReturnNotFoundWhenUserNotExists() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = inusController.asociarUsuarioConPrestador(UID_TEST, new HashMap<>());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void asociarUsuarioConPrestadorShouldReturnBadRequestWhenParamsMissing() {
        when(inusService.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Object>> response = inusController.asociarUsuarioConPrestador(UID_TEST, new HashMap<>());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void asociarUsuarioConPrestadorShouldReturnErrorWhenException() {
        when(inusService.findByUid(UID_TEST)).thenThrow(new RuntimeException(ERROR_MSG));
        
        ResponseEntity<Map<String, Object>> response = inusController.asociarUsuarioConPrestador(UID_TEST, new HashMap<>());
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void eliminarAsociacionUsuarioPrestadorShouldDeleteWhenExists() {
        when(inusService.eliminarAsociacion(UID_TEST, 1L)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = inusController.eliminarAsociacionUsuarioPrestador(UID_TEST, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarAsociacionUsuarioPrestadorShouldReturnNotFoundWhenNotExists() {
        when(inusService.eliminarAsociacion(UID_TEST, 1L)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = inusController.eliminarAsociacionUsuarioPrestador(UID_TEST, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listarAsociacionesShouldReturnList() {
        when(inusService.findAllAsociaciones()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = inusController.listarAsociaciones();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
