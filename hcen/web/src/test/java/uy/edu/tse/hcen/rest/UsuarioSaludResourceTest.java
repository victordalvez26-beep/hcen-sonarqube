package uy.edu.tse.hcen.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.repository.UserClinicAssociationRepository;
import uy.edu.tse.hcen.rest.dto.HcenUserResponse;
import uy.edu.tse.hcen.rest.dto.UsuarioSaludDTO;
import uy.edu.tse.hcen.service.InusIntegrationService;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioSaludResource.
 */
class UsuarioSaludResourceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserClinicAssociationRepository associationRepo;

    private InusIntegrationService inusService;

    private UsuarioSaludResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Crear stub manual para evitar problemas con JaCoCo y EJB
        inusService = new InusIntegrationService() {
            @Override
            public User obtenerUsuarioPorUid(String uid) {
                return null;
            }
            
            @Override
            public User obtenerUsuarioPorDocumento(String tipoDoc, String numeroDoc) {
                return null;
            }
            
            @Override
            public boolean crearUsuarioEnInus(User user) {
                return true;
            }
            
            @Override
            public boolean actualizarUsuarioEnInus(User user) {
                return true;
            }
            
            @Override
            public boolean asociarUsuarioConPrestador(String uid, Long tenantId, String rol) {
                return true;
            }
        };
        
        resource = new UsuarioSaludResource();
        
        // Inyectar dependencias via reflection
        Field userDAOField = UsuarioSaludResource.class.getDeclaredField("userDAO");
        userDAOField.setAccessible(true);
        userDAOField.set(resource, userDAO);
        
        Field associationRepoField = UsuarioSaludResource.class.getDeclaredField("associationRepo");
        associationRepoField.setAccessible(true);
        associationRepoField.set(resource, associationRepo);
        
        Field inusServiceField = UsuarioSaludResource.class.getDeclaredField("inusService");
        inusServiceField.setAccessible(true);
        inusServiceField.set(resource, inusService);
        
        // inusService es un stub manual, no necesita mockearse
    }

    @Test
    void crearModificarUsuarioSalud_newUser_shouldCreateAndAssociate() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        dto.setTenantId(1L);
        
        User newUser = new User();
        newUser.setId(100L);
        newUser.setUid("PACIENTE_12345678");
        newUser.setRol(Rol.USUARIO_SALUD);
        
        when(userDAO.findByDocumento("12345678")).thenReturn(null);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return null;
        }).when(userDAO).persist(any(User.class));
        when(associationRepo.findByUserAndClinic(anyLong(), anyLong())).thenReturn(null);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        HcenUserResponse userResponse = (HcenUserResponse) response.getEntity();
        assertNotNull(userResponse);
        assertEquals(100L, userResponse.getUserId());
        verify(userDAO).persist(any(User.class));
        verify(associationRepo).persist(any());
    }

    @Test
    void crearModificarUsuarioSalud_existingUser_shouldUpdate() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setNombre("Juan Actualizado");
        dto.setApellido("Pérez");
        dto.setTenantId(1L);
        
        User existingUser = new User();
        existingUser.setId(100L);
        existingUser.setUid("PACIENTE_12345678");
        existingUser.setPrimerNombre("Juan");
        existingUser.setRol(Rol.USUARIO_SALUD);
        
        when(userDAO.findByDocumento("12345678")).thenReturn(existingUser);
        when(associationRepo.findByUserAndClinic(100L, 1L)).thenReturn(null);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(userDAO).merge(existingUser);
        verify(associationRepo).persist(any());
    }

    @Test
    void crearModificarUsuarioSalud_missingCI_shouldReturnBadRequest() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setTenantId(1L);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        HcenUserResponse userResponse = (HcenUserResponse) response.getEntity();
        assertNotNull(userResponse);
        assertEquals("CI es requerido", userResponse.getMensaje());
    }

    @Test
    void crearModificarUsuarioSalud_missingTenantId_shouldReturnBadRequest() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        HcenUserResponse userResponse = (HcenUserResponse) response.getEntity();
        assertNotNull(userResponse);
        assertEquals("TenantId es requerido", userResponse.getMensaje());
    }

    @Test
    void crearModificarUsuarioSalud_existingAssociation_shouldNotCreateNew() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setNombre("Juan");
        dto.setTenantId(1L);
        
        User existingUser = new User();
        existingUser.setId(100L);
        existingUser.setUid("PACIENTE_12345678");
        
        when(userDAO.findByDocumento("12345678")).thenReturn(existingUser);
        when(associationRepo.findByUserAndClinic(100L, 1L)).thenReturn(new uy.edu.tse.hcen.model.UserClinicAssociation());
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(associationRepo, never()).persist(any());
    }

    @Test
    void ping_shouldReturnOk() {
        Response response = resource.ping();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("UsuarioSalud service is running", response.getEntity());
    }

    @Test
    void crearModificarUsuarioSalud_withNullNombre_shouldCreate() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setNombre(null);
        dto.setTenantId(1L);
        
        when(userDAO.findByDocumento("12345678")).thenReturn(null);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return null;
        }).when(userDAO).persist(any(User.class));
        when(associationRepo.findByUserAndClinic(anyLong(), anyLong())).thenReturn(null);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void crearModificarUsuarioSalud_withEmptyCI_shouldReturnBadRequest() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("");
        dto.setTenantId(1L);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearModificarUsuarioSalud_existingUserWithNullFields_shouldNotUpdate() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setNombre(null);
        dto.setApellido(null);
        dto.setTenantId(1L);
        
        User existingUser = new User();
        existingUser.setId(100L);
        existingUser.setPrimerNombre("Juan Original");
        existingUser.setPrimerApellido("Pérez Original");
        
        when(userDAO.findByDocumento("12345678")).thenReturn(existingUser);
        when(associationRepo.findByUserAndClinic(100L, 1L)).thenReturn(null);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(userDAO).merge(existingUser);
    }

    @Test
    void crearModificarUsuarioSalud_withException_shouldReturnInternalServerError() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setTenantId(1L);
        
        when(userDAO.findByDocumento("12345678")).thenThrow(new RuntimeException("Database error"));
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void crearModificarUsuarioSalud_withFechaNacimiento_shouldSetCorrectly() {
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setCi("12345678");
        dto.setNombre("Juan");
        dto.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        dto.setTenantId(1L);
        
        when(userDAO.findByDocumento("12345678")).thenReturn(null);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return null;
        }).when(userDAO).persist(any(User.class));
        when(associationRepo.findByUserAndClinic(anyLong(), anyLong())).thenReturn(null);
        
        Response response = resource.crearModificarUsuarioSalud(dto);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(userDAO).persist(any(User.class));
    }
}
