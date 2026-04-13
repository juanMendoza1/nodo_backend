package com.nodo.inv.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nodo.inv.core.dto.LoginRequestDTO;
import com.nodo.inv.core.dto.LoginResponseDTO;
import com.nodo.inv.core.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        return ResponseEntity.ok(authService.login(request));
    }
    
    @PreAuthorize("hasAuthority('INV_VIEW')")
    @GetMapping("/inventario")
    public String inventario() {
        return "📦 Inventario visible";
    }
}
