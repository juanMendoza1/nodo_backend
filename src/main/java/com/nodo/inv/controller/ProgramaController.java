package com.nodo.inv.controller;

import com.nodo.inv.dto.ProgramaDTO;
import com.nodo.inv.entity.Programa;
import com.nodo.inv.service.ProgramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
public class ProgramaController {

    private final ProgramaService programaService;

    // Cualquier usuario logueado (o al menos los administradores) deberían poder ver el catálogo
    @GetMapping
    public ResponseEntity<List<Programa>> listarTodos() {
        return ResponseEntity.ok(programaService.obtenerTodos());
    }

    // Solo el SUPER Admin puede crear nuevos módulos de software
    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crear(@RequestBody ProgramaDTO dto) {
        try {
            return ResponseEntity.ok(programaService.guardarPrograma(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ProgramaDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(programaService.guardarPrograma(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            programaService.eliminarPrograma(id);
            return ResponseEntity.ok(Map.of("message", "Programa eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}