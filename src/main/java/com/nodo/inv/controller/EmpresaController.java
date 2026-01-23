package com.nodo.inv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nodo.inv.dto.EmpresaRegistroDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.service.EmpresaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER')") // Solo tú como Super Admin puedes crear empresas
    public ResponseEntity<Empresa> registrar(@RequestBody EmpresaRegistroDTO dto) {
        return ResponseEntity.ok(empresaService.crearEmpresa(dto));
    }
}
