package com.nodo.inv.controller;

import com.nodo.inv.dto.EstructuraDTO;
import com.nodo.inv.entity.Estructura;
import com.nodo.inv.service.EstructuraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estructuras")
@RequiredArgsConstructor
public class EstructuraController {

    private final EstructuraService estructuraService;

    @GetMapping
    public ResponseEntity<List<Estructura>> listarTodas() {
        return ResponseEntity.ok(estructuraService.obtenerTodas());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> crear(@RequestBody EstructuraDTO dto) {
        try {
            return ResponseEntity.ok(estructuraService.guardarEstructura(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody EstructuraDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(estructuraService.guardarEstructura(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            estructuraService.eliminarEstructura(id);
            return ResponseEntity.ok(Map.of("message", "Estructura eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}