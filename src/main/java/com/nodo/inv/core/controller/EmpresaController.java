package com.nodo.inv.core.controller;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.dto.EmpresaDataDTO;
import com.nodo.inv.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // 🔥 Este es el método que te faltaba y generaba el GET not supported y el 403 Forbidden
    @GetMapping
    public ResponseEntity<List<Empresa>> listarTodas() {
        return ResponseEntity.ok(empresaService.obtenerTodas());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')") 
    public ResponseEntity<?> crear(@RequestBody EmpresaDataDTO dto) {
        try {
            return ResponseEntity.ok(empresaService.guardarEmpresa(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody EmpresaDataDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(empresaService.guardarEmpresa(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            empresaService.eliminarEmpresa(id);
            return ResponseEntity.ok(Map.of("message", "Empresa eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede eliminar porque tiene datos vinculados."));
        }
    }
}