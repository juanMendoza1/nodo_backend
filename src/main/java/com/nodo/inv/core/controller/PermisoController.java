package com.nodo.inv.core.controller;

import com.nodo.inv.core.entity.Permiso;
import com.nodo.inv.core.service.PermisoService; // 🔥 Importamos el servicio

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    // 🔥 Inyectamos el Servicio, no el repositorio
    private final PermisoService permisoService;

    @GetMapping
    public ResponseEntity<List<Permiso>> listarTodos() {
        return ResponseEntity.ok(permisoService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Permiso> crear(@RequestBody Permiso permiso) {
        return ResponseEntity.ok(permisoService.crear(permiso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permiso> actualizar(@PathVariable Long id, @RequestBody Permiso permiso) {
        return ResponseEntity.ok(permisoService.actualizar(id, permiso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        permisoService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    // 🔥 Endpoint especial para la gestión de dependencias (Fichas de Lego)
    @PutMapping("/{id}/dependencias")
    public ResponseEntity<Permiso> actualizarDependencias(@PathVariable Long id, @RequestBody List<Long> dependenciasIds) {
        return ResponseEntity.ok(permisoService.actualizarDependencias(id, dependenciasIds));
    }
}