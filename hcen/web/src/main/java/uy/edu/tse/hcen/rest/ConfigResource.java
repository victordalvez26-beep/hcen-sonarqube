package uy.edu.tse.hcen.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.model.Nacionalidad;
import uy.edu.tse.hcen.model.Rol;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    @GET
    @Path("/nacionalidades")
    public Response getNacionalidades() {
        List<NacionalidadDTO> nacionalidades = Arrays.stream(Nacionalidad.values())
                .map(n -> new NacionalidadDTO(n.getCodigo(), n.getNombre()))
                .collect(Collectors.toList());
        return Response.ok(nacionalidades).build();
    }

    @GET
    @Path("/roles")
    public Response getRoles() {
        List<RolDTO> roles = Arrays.stream(Rol.values())
                .map(r -> new RolDTO(r.getCodigo(), r.getDescripcion()))
                .collect(Collectors.toList());
        return Response.ok(roles).build();
    }

    // DTO para enviar la información de Nacionalidad al frontend
    public static class NacionalidadDTO {
        private String codigo;
        private String nombre;

        public NacionalidadDTO(String codigo, String nombre) {
            this.codigo = codigo;
            this.nombre = nombre;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }

    // DTO para enviar la información de Rol al frontend
    public static class RolDTO {
        private String codigo;
        private String descripcion;

        public RolDTO(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }
}