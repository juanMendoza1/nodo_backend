package com.nodo.inv.core.controller;

import com.nodo.inv.core.entity.GiroNegocio;
import com.nodo.inv.dto.GiroNegocioDTO;
import com.nodo.inv.service.GiroNegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/giros-negocio")
@RequiredArgsConstructor
public class GiroNegocioController {

    private final GiroNegocioService giroNegocioService;

    @GetMapping
    public ResponseEntity<List<GiroNegocio>> listarTodos() {
        return ResponseEntity.ok(giroNegocioService.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crear(@RequestBody GiroNegocioDTO dto) {
        return ResponseEntity.ok(giroNegocioService.guardar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody GiroNegocioDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(giroNegocioService.guardar(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        giroNegocioService.eliminar(id);
        return ResponseEntity.ok(Map.of("message", "Eliminado correctamente"));
    }
}