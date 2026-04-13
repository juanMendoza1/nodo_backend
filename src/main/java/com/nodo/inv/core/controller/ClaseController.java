package com.nodo.inv.core.controller;

import com.nodo.inv.core.dto.ClaseDTO;
import com.nodo.inv.core.entity.Clase;
import com.nodo.inv.core.service.ClaseService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseController {

    private final ClaseService claseService;

    @GetMapping
    public ResponseEntity<List<Clase>> listarTodas() {
        return ResponseEntity.ok(claseService.obtenerTodas());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> crear(@RequestBody ClaseDTO dto) {
        try {
            return ResponseEntity.ok(claseService.guardarClase(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ClaseDTO dto) {
        try {
            dto.setId(id); // Aseguramos que el ID de la URL se inyecte en el DTO
            return ResponseEntity.ok(claseService.guardarClase(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            claseService.eliminarClase(id);
            return ResponseEntity.ok(Map.of("message", "Clase eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}