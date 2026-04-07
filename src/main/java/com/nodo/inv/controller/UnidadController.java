// src/main/java/com/nodo/inv/controller/UnidadController.java
package com.nodo.inv.controller;

import com.nodo.inv.dto.UnidadDTO;
import com.nodo.inv.entity.Unidad;
import com.nodo.inv.service.UnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadController {

    private final UnidadService unidadService;

    // 🔥 NUEVO: Requiere la empresa por URL
    @GetMapping("/estructura/{codigo}/empresa/{empresaId}")
    public ResponseEntity<List<Unidad>> listarPorEstructuraYEmpresa(
            @PathVariable String codigo, 
            @PathVariable Long empresaId) {
        return ResponseEntity.ok(unidadService.obtenerPorEstructuraYEmpresa(codigo, empresaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> guardar(@RequestBody UnidadDTO dto) {
        try {
            return ResponseEntity.ok(unidadService.guardarUnidad(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            unidadService.eliminarUnidad(id);
            return ResponseEntity.ok(Map.of("mensaje", "Parámetro eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<List<Unidad>> listarTodas() {
        return ResponseEntity.ok(unidadService.obtenerTodas());
    }
}