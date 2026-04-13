package com.nodo.inv.core.controller;

import com.nodo.inv.core.entity.Concepto;
import com.nodo.inv.core.repository.ConceptoRepository;
import com.nodo.inv.service.ConceptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conceptos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConceptoController {

    private final ConceptoService conceptoService;
    private final ConceptoRepository conceptoRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Concepto>> obtenerTodos() {
        return ResponseEntity.ok(conceptoRepository.findAll());
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Concepto>> obtenerDisponibles(
            @RequestParam Long empresaId,
            @RequestParam Long programaId) {
        return ResponseEntity.ok(conceptoRepository.findDisponiblesPorEmpresaYPrograma(empresaId, programaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> crearConcepto(@RequestBody Concepto concepto) {
        try {
            return ResponseEntity.ok(conceptoService.guardarConcepto(concepto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> actualizarConcepto(@PathVariable Long id, @RequestBody Concepto concepto) {
        try {
            concepto.setId(id);
            return ResponseEntity.ok(conceptoService.guardarConcepto(concepto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> eliminarConcepto(@PathVariable Long id) {
        try {
            conceptoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Concepto eliminado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar. Es probable que esté en uso en alguna Matriz de Cálculo o Documento."));
        }
    }
}