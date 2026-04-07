package com.nodo.inv.controller;

import com.nodo.inv.dto.SuscripcionDTO;
import com.nodo.inv.dto.SuscripcionGuardarDTO;
import com.nodo.inv.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<List<SuscripcionDTO>> listarTodas() {
        return ResponseEntity.ok(suscripcionService.obtenerTodas());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crear(@RequestBody SuscripcionGuardarDTO dto) {
        try {
            return ResponseEntity.ok(suscripcionService.guardarSuscripcion(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody SuscripcionGuardarDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(suscripcionService.guardarSuscripcion(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            suscripcionService.eliminarSuscripcion(id);
            return ResponseEntity.ok(Map.of("message", "Suscripción eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}