// src/main/java/com/nodo/inv/controller/RolController.java
package com.nodo.inv.controller;

import com.nodo.inv.dto.RolDTO;
import com.nodo.inv.entity.Rol;
import com.nodo.inv.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<List<Rol>> listarTodos() {
        return ResponseEntity.ok(rolService.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crear(@RequestBody RolDTO dto) {
        try {
            return ResponseEntity.ok(rolService.guardar(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody RolDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(rolService.guardar(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            rolService.eliminar(id);
            return ResponseEntity.ok(Map.of("message", "Rol eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}