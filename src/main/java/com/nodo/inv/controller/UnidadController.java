package com.nodo.inv.controller;

import com.nodo.inv.entity.Unidad;
import com.nodo.inv.service.UnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadController {

    private final UnidadService unidadService;

    @GetMapping("/estructura/{codigo}")
    public ResponseEntity<List<Unidad>> listarPorEstructura(@PathVariable String codigo) {
        return ResponseEntity.ok(unidadService.obtenerPorEstructura(codigo));
    }
}