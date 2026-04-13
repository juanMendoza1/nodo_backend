package com.nodo.inv.retail.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nodo.inv.retail.dto.ProductoDTO;
import com.nodo.inv.retail.service.ProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<List<ProductoDTO>> listarCatalogo(@PathVariable Long empresaId) {
        return ResponseEntity.ok(productoService.obtenerCatalogoPorEmpresa(empresaId));
    }

    @PostMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> guardar(
            @PathVariable Long empresaId, 
            @RequestBody ProductoDTO dto) {
        try {
            return ResponseEntity.ok(productoService.guardarProducto(empresaId, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> toggleEstado(@PathVariable Long id) {
        try {
            productoService.cambiarEstado(id);
            return ResponseEntity.ok(Map.of("mensaje", "Estado del producto actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}