package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.Nacionalidad;
import uy.edu.tse.hcen.model.Departamento;
import uy.edu.tse.hcen.utils.InusServiceUrlUtil;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;

@Stateless
public class InusIntegrationService {

    private static final Logger LOGGER = Logger.getLogger(InusIntegrationService.class.getName());

    public User obtenerUsuarioPorUid(String uid) {
        String url = InusServiceUrlUtil.buildUrl("/api/inus/usuarios/" + uid);
        LOGGER.info("Consultando INUS por UID: " + url);
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                return mapJsonToUser(jsonResponse);
            } else {
                LOGGER.info("Usuario no encontrado en INUS o error: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            LOGGER.severe("Excepción al conectar con INUS: " + e.getMessage());
            return null;
        }
    }

    public User obtenerUsuarioPorDocumento(String tipoDocumento, String numeroDocumento) {
        String url = InusServiceUrlUtil.buildUrl("/api/inus/usuarios/buscar/nombre-fecha"); // Endpoint por defecto si no hay específico
        // TODO: Ajustar si existe endpoint específico por documento en INUS, actualmente simulamos búsqueda
        // El controller de INUS tiene endpoints por nombre/fecha, telefono, email.
        // Si agregamos endpoint por documento en INUS (que debería tener), usar ese.
        // Por ahora, asumimos que INUS tiene un endpoint /usuarios/buscar/documento o similar si lo agregamos
        // Pero el código original llamaba a /api/inus/usuarios/documento que no vi en el controller.
        
        // CORRECCION: El endpoint en PDI Controller actual no tiene búsqueda por documento explícita pública
        // excepto via UID si UID = documento. 
        // Si UserResource lo necesita, debería agregarse en PDI.
        
        LOGGER.warning("Búsqueda por documento en INUS no implementada completamente en cliente.");
        return null; 
    }

    public boolean crearUsuarioEnInus(User user) {
        String url = InusServiceUrlUtil.buildUrl("/api/inus/usuarios");
        LOGGER.info("Creando usuario en INUS: " + url);
        JsonObject jsonUser = mapUserToJson(user);
        
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(jsonUser.toString()));

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                LOGGER.info("Usuario creado exitosamente en INUS");
                return true;
            } else {
                LOGGER.severe("Error al crear usuario en INUS: " + response.getStatus() + " - " + response.readEntity(String.class));
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Excepción al crear usuario en INUS: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarUsuarioEnInus(User user) {
        if (user.getUid() == null) return false;
        
        String url = InusServiceUrlUtil.buildUrl("/api/inus/usuarios/" + user.getUid());
        LOGGER.info("Actualizando usuario en INUS: " + url);
        JsonObject jsonUser = mapUserToJson(user);

        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(jsonUser.toString()));

            if (response.getStatus() == 200) {
                LOGGER.info("Usuario actualizado exitosamente en INUS");
                return true;
            } else {
                LOGGER.severe("Error al actualizar usuario en INUS: " + response.getStatus());
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Excepción al actualizar usuario en INUS: " + e.getMessage());
            return false;
        }
    }
    
    public boolean asociarUsuarioConPrestador(String uid, Long prestadorId, String prestadorRut) {
        String url = InusServiceUrlUtil.buildUrl("/api/inus/usuarios/" + uid + "/prestadores");
        LOGGER.info("Asociando usuario con prestador en INUS: " + url);
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (prestadorId != null) builder.add("prestadorId", prestadorId);
        if (prestadorRut != null) builder.add("prestadorRut", prestadorRut);
        JsonObject requestBody = builder.build();

        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody.toString()));

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                LOGGER.info("Asociación creada exitosamente en INUS");
                return true;
            } else {
                String errorMsg = response.readEntity(String.class);
                // Si ya existe (409 Conflict o similar, o mensaje específico), lo consideramos éxito o warning
                if (response.getStatus() == 409 || errorMsg.contains("ya existe")) {
                     LOGGER.info("La asociación ya existía en INUS.");
                     return true;
                }
                LOGGER.severe("Error al crear asociación en INUS: " + response.getStatus() + " - " + errorMsg);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Excepción al crear asociación en INUS: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<User> listarUsuarios() {
        String url = InusServiceUrlUtil.buildUrl("/api/inus/usuarios");
        LOGGER.info("Listando usuarios de INUS: " + url);
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                return mapJsonToUserList(jsonResponse);
            } else {
                LOGGER.severe("Error al listar usuarios de INUS: " + response.getStatus());
                return new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.severe("Excepción al listar usuarios de INUS: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    private java.util.List<User> mapJsonToUserList(String jsonString) {
        java.util.List<User> users = new java.util.ArrayList<>();
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            JsonObject json = jsonReader.readObject();
            if (json.containsKey("usuarios") && !json.isNull("usuarios")) {
                jakarta.json.JsonArray usuariosArray = json.getJsonArray("usuarios");
                for (jakarta.json.JsonValue val : usuariosArray) {
                    JsonObject userJson = val.asJsonObject();
                    User user = mapJsonObjectToUser(userJson);
                    if (user != null) users.add(user);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error mapeando lista de usuarios: " + e.getMessage());
        }
        return users;
    }

    private User mapJsonToUser(String jsonString) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            JsonObject json = jsonReader.readObject();

            // INUS returns wrapped response: {"success":true, "usuario": {...}}
            if (json.containsKey("usuario") && !json.isNull("usuario")) {
                json = json.getJsonObject("usuario");
            }
            
            return mapJsonObjectToUser(json);
        } catch (Exception e) {
            LOGGER.severe("Error mapeando JSON a User: " + e.getMessage());
            return null;
        }
    }

    private User mapJsonObjectToUser(JsonObject json) {
        try {
            User user = new User();
            
            if (json.containsKey("uid") && !json.isNull("uid")) user.setUid(json.getString("uid"));
            if (json.containsKey("email") && !json.isNull("email")) user.setEmail(json.getString("email"));
            if (json.containsKey("primerNombre") && !json.isNull("primerNombre")) user.setPrimerNombre(json.getString("primerNombre"));
            if (json.containsKey("segundoNombre") && !json.isNull("segundoNombre")) user.setSegundoNombre(json.getString("segundoNombre"));
            if (json.containsKey("primerApellido") && !json.isNull("primerApellido")) user.setPrimerApellido(json.getString("primerApellido"));
            if (json.containsKey("segundoApellido") && !json.isNull("segundoApellido")) user.setSegundoApellido(json.getString("segundoApellido"));
            if (json.containsKey("tipDocum") && !json.isNull("tipDocum")) user.setTipDocum(json.getString("tipDocum"));
            if (json.containsKey("codDocum") && !json.isNull("codDocum")) user.setCodDocum(json.getString("codDocum"));
            
            if (json.containsKey("nacionalidad") && !json.isNull("nacionalidad")) {
                try {
                    user.setNacionalidad(Nacionalidad.valueOf(json.getString("nacionalidad")));
                } catch (IllegalArgumentException e) {
                    // Ignore or log
                }
            }
            
            if (json.containsKey("fechaNacimiento") && !json.isNull("fechaNacimiento")) {
                String fechaStr = json.getString("fechaNacimiento");
                // INUS uses LocalDate (yyyy-MM-dd), HCEN uses Date
                try {
                    LocalDate ld = LocalDate.parse(fechaStr);
                    user.setFechaNacimiento(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                } catch (Exception e) {
                    LOGGER.warning("Error parseando fecha nacimiento de INUS: " + fechaStr);
                }
            }
            
            if (json.containsKey("departamento") && !json.isNull("departamento")) {
                try {
                    user.setDepartamento(Departamento.valueOf(json.getString("departamento")));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
            
            if (json.containsKey("localidad") && !json.isNull("localidad")) user.setLocalidad(json.getString("localidad"));
            if (json.containsKey("direccion") && !json.isNull("direccion")) user.setDireccion(json.getString("direccion"));
            if (json.containsKey("telefono") && !json.isNull("telefono")) user.setTelefono(json.getString("telefono"));
            if (json.containsKey("codigoPostal") && !json.isNull("codigoPostal")) user.setCodigoPostal(json.getString("codigoPostal"));
            
            return user;
        } catch (Exception e) {
            LOGGER.severe("Error mapeando JsonObject a User: " + e.getMessage());
            return null;
        }
    }

    private JsonObject mapUserToJson(User user) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        if (user.getUid() != null) builder.add("uid", user.getUid());
        if (user.getEmail() != null) builder.add("email", user.getEmail());
        if (user.getPrimerNombre() != null) builder.add("primerNombre", user.getPrimerNombre());
        if (user.getSegundoNombre() != null) builder.add("segundoNombre", user.getSegundoNombre());
        if (user.getPrimerApellido() != null) builder.add("primerApellido", user.getPrimerApellido());
        if (user.getSegundoApellido() != null) builder.add("segundoApellido", user.getSegundoApellido());
        if (user.getTipDocum() != null) builder.add("tipDocum", user.getTipDocum());
        if (user.getCodDocum() != null) builder.add("codDocum", user.getCodDocum());
        if (user.getNacionalidad() != null) builder.add("nacionalidad", user.getNacionalidad().name());
        
        if (user.getFechaNacimiento() != null) {
            LocalDate ld = user.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            builder.add("fechaNacimiento", ld.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        if (user.getDepartamento() != null) builder.add("departamento", user.getDepartamento().name());
        if (user.getLocalidad() != null) builder.add("localidad", user.getLocalidad());
        if (user.getDireccion() != null) builder.add("direccion", user.getDireccion());
        if (user.getTelefono() != null) builder.add("telefono", user.getTelefono());
        if (user.getCodigoPostal() != null) builder.add("codigoPostal", user.getCodigoPostal());
        
        return builder.build();
    }
}