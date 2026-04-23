package com.nodo.inv.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // 🔥 Importante
import org.springframework.web.bind.annotation.*;

import com.nodo.inv.core.dto.LoginRequestDTO;
import com.nodo.inv.core.dto.LoginResponseDTO;
import com.nodo.inv.core.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") 
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }    
    
    @PostMapping("/switch-context/{empresaId}")
    public ResponseEntity<LoginResponseDTO> switchContext(@PathVariable Long empresaId, Authentication authentication) {
        
     
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.ok(authService.switchContext(empresaId, authentication.getName()));
    }
    
    @PreAuthorize("hasAuthority('INV_VIEW')")
    @GetMapping("/inventario")
    public String inventario() {
        return "Inventario visible";
    }
}