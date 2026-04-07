package com.nodo.inv.controller;

import com.nodo.inv.entity.Permiso;
import com.nodo.inv.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoRepository permisoRepository;

    @GetMapping
    public ResponseEntity<List<Permiso>> listarTodos() {
        return ResponseEntity.ok(permisoRepository.findAll());
    }
}