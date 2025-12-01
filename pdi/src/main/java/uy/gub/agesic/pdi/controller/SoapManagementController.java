package uy.gub.agesic.pdi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uy.gub.agesic.pdi.model.PersonaData;
import uy.gub.agesic.pdi.repository.PersonaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/soap/personas")
@CrossOrigin(origins = "*") // Permitir acceso desde cualquier origen para facilitar pruebas
public class SoapManagementController {

    @Autowired
    private PersonaRepository personaRepository;

    /**
     * Listar todas las personas disponibles en el servicio SOAP simulado
     */
    @GetMapping
    public ResponseEntity<List<PersonaData>> listarPersonas() {
        return ResponseEntity.ok(personaRepository.findAll());
    }

    /**
     * Agregar una nueva persona a la base de datos simulada
     */
    @PostMapping
    public ResponseEntity<?> agregarPersona(@RequestBody PersonaData persona) {
        try {
            // Validaciones básicas
            if (persona.getTipoDocumento() == null || persona.getNumeroDocumento() == null) {
                return ResponseEntity.badRequest().body("Tipo y Número de documento son obligatorios");
            }
            
            // Verificar si ya existe
            if (personaRepository.findByDocumento(persona.getTipoDocumento(), persona.getNumeroDocumento()).isPresent()) {
                return ResponseEntity.status(409).body("Ya existe una persona con ese documento");
            }
            
            personaRepository.addPersona(persona);
            return ResponseEntity.ok("Persona agregada correctamente al servicio SOAP simulado");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al agregar persona: " + e.getMessage());
        }
    }
}

