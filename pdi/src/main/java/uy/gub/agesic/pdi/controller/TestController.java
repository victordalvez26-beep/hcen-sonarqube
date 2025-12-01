package uy.gub.agesic.pdi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uy.gub.agesic.pdi.model.PersonaData;
import uy.gub.agesic.pdi.repository.PersonaRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para testing y administración
 * Permite agregar personas al repositorio en memoria
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private PersonaRepository personaRepository;

    /**
     * Agregar una nueva persona al repositorio
     */
    @PostMapping("/personas")
    public ResponseEntity<Map<String, Object>> agregarPersona(@RequestBody PersonaData persona) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            personaRepository.addPersona(persona);
            
            response.put("success", true);
            response.put("mensaje", "Persona agregada exitosamente");
            response.put("persona", persona);
            response.put("totalPersonas", personaRepository.findAll().size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", "Error al agregar persona: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar todas las personas (para verificación)
     */
    @GetMapping("/personas")
    public ResponseEntity<Map<String, Object>> listarPersonas() {
        Map<String, Object> response = new HashMap<>();
        response.put("total", personaRepository.findAll().size());
        response.put("personas", personaRepository.findAll());
        return ResponseEntity.ok(response);
    }
}

