package com.nodo.inv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nodo.inv.dto.UsuarioRegistroDTO;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER')") 
    public ResponseEntity<Usuario> crear(@RequestBody UsuarioRegistroDTO dto) {
        return ResponseEntity.ok(usuarioService.crearUsuarioAdmin(dto));
    }
}