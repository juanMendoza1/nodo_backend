package com.nodo.inv.core.controller;

import com.nodo.inv.core.dto.DominioOperativoDTO;
import com.nodo.inv.core.entity.DominioOperativo;
import com.nodo.inv.core.service.DominioOperativoService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dominios-operativos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DominioOperativoController {

    private final DominioOperativoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<DominioOperativo>> listarTodos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crear(@RequestBody DominioOperativoDTO dto) {
        try {
            return ResponseEntity.ok(service.guardar(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody DominioOperativoDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(service.guardar(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok(Map.of("message", "Dominio eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede eliminar porque está siendo usado por uno o más Programas (SaaS)."));
        }
    }
}