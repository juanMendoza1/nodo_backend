package com.nodo.inv.core.controller;

import com.nodo.inv.core.dto.PeriodoFacturacionDTO;
import com.nodo.inv.core.entity.PeriodoFacturacion;
import com.nodo.inv.core.service.PeriodoFacturacionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/periodos-facturacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PeriodoFacturacionController {

    private final PeriodoFacturacionService periodoService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> obtenerPeriodo(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(periodoService.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizarPeriodo(@PathVariable Long id, @RequestBody PeriodoFacturacionDTO dto) {
        try {
            PeriodoFacturacion actualizado = periodoService.actualizarPeriodo(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}