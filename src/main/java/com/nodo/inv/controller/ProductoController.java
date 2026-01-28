package com.nodo.inv.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nodo.inv.dto.ProductoDTO;
import com.nodo.inv.entity.Producto;
import com.nodo.inv.service.ProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ProductoDTO>> listarCatalogo(@PathVariable Long empresaId) {
        return ResponseEntity.ok(productoService.obtenerCatalogoPorEmpresa(empresaId));
    }
}