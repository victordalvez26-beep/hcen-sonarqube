package uy.gub.agesic.inus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uy.gub.agesic.inus.model.InusUsuario;
import uy.gub.agesic.inus.model.UsuarioPrestadorAssociation;
import uy.gub.agesic.inus.repository.InusUsuarioRepository;
import uy.gub.agesic.inus.repository.UsuarioPrestadorRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InusServiceTest {

    private static final String UID_TEST = "uid-test";
    private static final String RUT_TEST = "12345678";
    private static final String NON_EXISTENT = "non-existent";
    private static final String RUT = "rut";
    private static final String UID = "uid";
    private static final String UPDATED_EMAIL = "updated@example.com";

    @Mock
    private InusUsuarioRepository usuarioRepo;

    @Mock
    private UsuarioPrestadorRepository asociacionRepo;

    @InjectMocks
    private InusService inusService;

    private InusUsuario testUser;
    private UsuarioPrestadorAssociation testAsoc;

    @BeforeEach
    void setUp() {
        testUser = new InusUsuario();
        testUser.setId(1L);
        testUser.setUid(UID_TEST);
        testUser.setPrimerNombre("Test");
        testUser.setPrimerApellido("User");
        testUser.setEmail("test@example.com");

        testAsoc = new UsuarioPrestadorAssociation(UID_TEST, 1L, RUT_TEST);
        testAsoc.setId(1L);
    }

    @Test
    void initShouldLoadDataWhenRepoIsEmpty() {
        when(usuarioRepo.count()).thenReturn(0L);
        
        inusService.init();
        
        verify(usuarioRepo, atLeastOnce()).save(any(InusUsuario.class));
        verify(asociacionRepo, atLeastOnce()).save(any(UsuarioPrestadorAssociation.class));
    }

    @Test
    void initShouldNotLoadDataWhenRepoIsNotEmpty() {
        when(usuarioRepo.count()).thenReturn(5L);
        
        inusService.init();
        
        verify(usuarioRepo, never()).save(any(InusUsuario.class));
    }

    @Test
    void findByUidShouldReturnUserWhenExists() {
        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        
        Optional<InusUsuario> result = inusService.findByUid(UID_TEST);
        
        assertTrue(result.isPresent());
        assertEquals(UID_TEST, result.get().getUid());
    }

    @Test
    void saveShouldCreateNewUser() {
        when(usuarioRepo.save(any(InusUsuario.class))).thenReturn(testUser);
        
        InusUsuario result = inusService.save(testUser);
        
        assertNotNull(result);
        assertEquals(UID_TEST, result.getUid());
    }

    @Test
    void saveShouldUpdateIdWhenUidExists() {
        InusUsuario newUser = new InusUsuario();
        newUser.setUid(UID_TEST);
        
        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        when(usuarioRepo.save(any(InusUsuario.class))).thenReturn(testUser);
        
        inusService.save(newUser);
        
        assertEquals(1L, newUser.getId());
    }

    @Test
    void updateShouldUpdateFieldsWhenUserExists() {
        InusUsuario updateData = new InusUsuario();
        updateData.setPrimerNombre("Updated");
        updateData.setEmail(UPDATED_EMAIL);
        
        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        when(usuarioRepo.save(any(InusUsuario.class))).thenAnswer(i -> i.getArguments()[0]);
        
        Optional<InusUsuario> result = inusService.update(UID_TEST, updateData);
        
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getPrimerNombre());
        assertEquals(UPDATED_EMAIL, result.get().getEmail());
        assertEquals("User", result.get().getPrimerApellido()); // Should remain unchanged
    }

    @Test
    void updateShouldReturnEmptyWhenUserDoesNotExist() {
        when(usuarioRepo.findByUid(NON_EXISTENT)).thenReturn(Optional.empty());
        
        Optional<InusUsuario> result = inusService.update(NON_EXISTENT, new InusUsuario());
        
        assertFalse(result.isPresent());
    }

    @Test
    void deleteByUidShouldReturnTrueWhenUserExists() {
        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        
        boolean result = inusService.deleteByUid(UID_TEST);
        
        assertTrue(result);
        verify(usuarioRepo).delete(testUser);
    }

    @Test
    void deleteByUidShouldReturnFalseWhenUserDoesNotExist() {
        when(usuarioRepo.findByUid(NON_EXISTENT)).thenReturn(Optional.empty());
        
        boolean result = inusService.deleteByUid(NON_EXISTENT);
        
        assertFalse(result);
        verify(usuarioRepo, never()).delete(any());
    }

    @Test
    void crearAsociacionShouldCreateWhenNotExists() {
        when(asociacionRepo.findByUsuarioUidAndPrestadorId(UID_TEST, 1L)).thenReturn(Optional.empty());
        when(asociacionRepo.save(any(UsuarioPrestadorAssociation.class))).thenReturn(testAsoc);
        
        UsuarioPrestadorAssociation result = inusService.crearAsociacion(UID_TEST, 1L, RUT_TEST);
        
        assertNotNull(result);
        assertEquals(UID_TEST, result.getUsuarioUid());
    }

    @Test
    void crearAsociacionShouldReturnExistingWhenExists() {
        when(asociacionRepo.findByUsuarioUidAndPrestadorId(UID_TEST, 1L)).thenReturn(Optional.of(testAsoc));
        
        UsuarioPrestadorAssociation result = inusService.crearAsociacion(UID_TEST, 1L, RUT_TEST);
        
        assertEquals(testAsoc, result);
        verify(asociacionRepo, never()).save(any());
    }

    @Test
    void findUsuariosCompletosByPrestadorIdShouldReturnUsers() {
        when(asociacionRepo.findByPrestadorId(1L)).thenReturn(Arrays.asList(testAsoc));
        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        
        List<InusUsuario> result = inusService.findUsuariosCompletosByPrestadorId(1L);
        
        assertEquals(1, result.size());
        assertEquals(UID_TEST, result.get(0).getUid());
    }
    
    @Test
    void findAllShouldReturnList() {
        when(usuarioRepo.findAll()).thenReturn(Arrays.asList(testUser));
        List<InusUsuario> result = inusService.findAll();
        assertEquals(1, result.size());
    }
    
    @Test
    void findByDocumentoShouldReturnUser() {
        when(usuarioRepo.findByTipDocumAndCodDocum("CI", "123")).thenReturn(Optional.of(testUser));
        Optional<InusUsuario> result = inusService.findByDocumento("CI", "123");
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByTipDocumAndCodDocumShouldReturnUser() {
        when(usuarioRepo.findByTipDocumAndCodDocum("CI", "123")).thenReturn(Optional.of(testUser));
        Optional<InusUsuario> result = inusService.findByTipDocumAndCodDocum("CI", "123");
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByNombreApellidoAndFechaNacimientoShouldReturnUser() {
        LocalDate now = LocalDate.now();
        when(usuarioRepo.findByNombreApellidoFecha("Test", "User", now)).thenReturn(Optional.of(testUser));
        Optional<InusUsuario> result = inusService.findByNombreApellidoAndFechaNacimiento("Test", "User", now);
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByNombreApellidoAndTelefonoShouldReturnUser() {
        when(usuarioRepo.findByNombreApellidoTelefono("Test", "User", "123")).thenReturn(Optional.of(testUser));
        Optional<InusUsuario> result = inusService.findByNombreApellidoAndTelefono("Test", "User", "123");
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByNombreApellidoAndEmailShouldReturnUser() {
        when(usuarioRepo.findByNombreApellidoEmail("Test", "User", "email")).thenReturn(Optional.of(testUser));
        Optional<InusUsuario> result = inusService.findByNombreApellidoAndEmail("Test", "User", "email");
        assertTrue(result.isPresent());
    }
    
    @Test
    void existsByUidShouldReturnTrue() {
        when(usuarioRepo.existsByUid(UID)).thenReturn(true);
        assertTrue(inusService.existsByUid(UID));
    }
    
    @Test
    void findAsociacionByUsuarioYPrestadorShouldReturnAsoc() {
        when(asociacionRepo.findByUsuarioUidAndPrestadorId(UID, 1L)).thenReturn(Optional.of(testAsoc));
        Optional<UsuarioPrestadorAssociation> result = inusService.findAsociacionByUsuarioYPrestador(UID, 1L);
        assertTrue(result.isPresent());
    }
    
    @Test
    void findPrestadoresByUsuarioShouldReturnList() {
        when(asociacionRepo.findByUsuarioUid(UID)).thenReturn(Arrays.asList(testAsoc));
        List<Long> result = inusService.findPrestadoresByUsuario(UID);
        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0));
    }
    
    @Test
    void findUsuariosByPrestadorRutShouldReturnList() {
        when(asociacionRepo.findByPrestadorRut(RUT)).thenReturn(Arrays.asList(testAsoc));
        List<String> result = inusService.findUsuariosByPrestadorRut(RUT);
        assertFalse(result.isEmpty());
        assertEquals(UID_TEST, result.get(0));
    }
    
    @Test
    void findUsuariosCompletosByPrestadorRutShouldReturnList() {
        when(asociacionRepo.findByPrestadorRut(RUT)).thenReturn(Arrays.asList(testAsoc));
        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        List<InusUsuario> result = inusService.findUsuariosCompletosByPrestadorRut(RUT);
        assertFalse(result.isEmpty());
    }
    
    @Test
    void eliminarAsociacionShouldDeleteWhenExists() {
        when(asociacionRepo.findByUsuarioUidAndPrestadorId(UID, 1L)).thenReturn(Optional.of(testAsoc));
        boolean result = inusService.eliminarAsociacion(UID, 1L);
        assertTrue(result);
        verify(asociacionRepo).delete(testAsoc);
    }
    
    @Test
    void eliminarAsociacionShouldReturnFalseWhenNotExists() {
        when(asociacionRepo.findByUsuarioUidAndPrestadorId(UID, 1L)).thenReturn(Optional.empty());
        boolean result = inusService.eliminarAsociacion(UID, 1L);
        assertFalse(result);
    }
    
    @Test
    void findAllAsociacionesShouldReturnList() {
        when(asociacionRepo.findAll()).thenReturn(Arrays.asList(testAsoc));
        List<UsuarioPrestadorAssociation> result = inusService.findAllAsociaciones();
        assertFalse(result.isEmpty());
    }

    @Test
    void updateShouldUpdateAllFields() {
        InusUsuario updateData = new InusUsuario();
        updateData.setPrimerNombre("UpdatedName");
        updateData.setSegundoNombre("UpdatedSecondName");
        updateData.setPrimerApellido("UpdatedSurname");
        updateData.setSegundoApellido("UpdatedSecondSurname");
        updateData.setTipDocum("PASAPORTE");
        updateData.setCodDocum("999999");
        updateData.setNacionalidad(uy.gub.agesic.inus.model.Nacionalidad.BR);
        updateData.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        updateData.setEmail(UPDATED_EMAIL);
        updateData.setDepartamento(uy.gub.agesic.inus.model.Departamento.CANELONES);
        updateData.setLocalidad("Canelones");
        updateData.setDireccion("New Address");
        updateData.setTelefono("099999999");
        updateData.setCodigoPostal("90000");
        updateData.setRol(uy.gub.agesic.inus.model.Rol.ADMIN_HCEN);
        updateData.setProfileCompleted(true);

        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        when(usuarioRepo.save(any(InusUsuario.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<InusUsuario> result = inusService.update(UID_TEST, updateData);

        assertTrue(result.isPresent());
        InusUsuario updated = result.get();
        assertEquals("UpdatedName", updated.getPrimerNombre());
        assertEquals("UpdatedSecondName", updated.getSegundoNombre());
        assertEquals("UpdatedSurname", updated.getPrimerApellido());
        assertEquals("UpdatedSecondSurname", updated.getSegundoApellido());
        assertEquals("PASAPORTE", updated.getTipDocum());
        assertEquals("999999", updated.getCodDocum());
        assertEquals(uy.gub.agesic.inus.model.Nacionalidad.BR, updated.getNacionalidad());
        assertEquals(LocalDate.of(1990, 1, 1), updated.getFechaNacimiento());
        assertEquals(UPDATED_EMAIL, updated.getEmail());
        assertEquals(uy.gub.agesic.inus.model.Departamento.CANELONES, updated.getDepartamento());
        assertEquals("Canelones", updated.getLocalidad());
        assertEquals("New Address", updated.getDireccion());
        assertEquals("099999999", updated.getTelefono());
        assertEquals("90000", updated.getCodigoPostal());
        assertEquals(uy.gub.agesic.inus.model.Rol.ADMIN_HCEN, updated.getRol());
        assertTrue(updated.isProfileCompleted());
    }

    @Test
    void updateShouldNotUpdateNullFields() {
        InusUsuario updateData = new InusUsuario();
        // All fields null by default

        when(usuarioRepo.findByUid(UID_TEST)).thenReturn(Optional.of(testUser));
        when(usuarioRepo.save(any(InusUsuario.class))).thenAnswer(i -> i.getArguments()[0]);

        Optional<InusUsuario> result = inusService.update(UID_TEST, updateData);

        assertTrue(result.isPresent());
        InusUsuario updated = result.get();
        assertEquals("Test", updated.getPrimerNombre()); // Should remain unchanged
        assertEquals("User", updated.getPrimerApellido()); // Should remain unchanged
    }
}
