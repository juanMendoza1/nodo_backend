package com.nodo.inv.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nodo.inv.dto.UsuarioRegistroDTO;
import com.nodo.inv.dto.UsuarioSlotDTO;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.entity.UsuarioOperativo;
import com.nodo.inv.service.UsuarioOperativoService;
import com.nodo.inv.service.UsuarioService;
import com.nodo.inv.dto.UsuarioSlotGuardarDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioOperativoService operativoService;

    // 1. Crear usuarios (Lo que ya tenías)
    @PostMapping
    @PreAuthorize("hasRole('SUPER')") 
    public ResponseEntity<Usuario> crear(@RequestBody UsuarioRegistroDTO dto) {
        return ResponseEntity.ok(usuarioService.crearUsuarioAdmin(dto));
    }

    // 2. NUEVO: Obtener los "Slots" para la tablet
    // Este endpoint lo llamará la tablet justo después de vincularse
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<UsuarioSlotDTO>> listarSlotsPorEmpresa(@PathVariable Long empresaId) {
        List<UsuarioOperativo> usuarios = operativoService.listarPorEmpresa(empresaId);
        
        // Mapeamos de UsuarioOperativo a nuestro DTO para Android
        List<UsuarioSlotDTO> slots = usuarios.stream()
                .map(u -> new UsuarioSlotDTO(
                        u.getId(), 
                        u.getAlias(), // Usamos el ALIAS como nombre para la tablet
                        u.getLogin(),
                        u.getPassword(),
                        u.getRol().getNombre(),
                        u.getEstado().name()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/login-tablet")
    public ResponseEntity<?> loginTablet(@RequestBody Map<String, Object> credenciales) {
        try {
            // Extraemos los parámetros con seguridad
            Long usuarioId = Long.valueOf(credenciales.get("usuarioId").toString());
            Long empresaId = Long.valueOf(credenciales.get("empresaId").toString());
            String pin = credenciales.get("pin").toString();

            // Llamamos al servicio con validación de identidad y pertenencia
            boolean valido = operativoService.verificarAccesoSeguro(usuarioId, empresaId, pin);

            if (valido) {
                return ResponseEntity.ok(Map.of("status", "success", "mensaje", "Acceso concedido"));
            } else {
                return ResponseEntity.status(401).body("Credenciales inválidas para esta empresa");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en la solicitud de autenticación");
        }
    }
    
    @PostMapping("/empresa/{empresaId}/slots")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> guardarSlot(@PathVariable Long empresaId, @RequestBody UsuarioSlotGuardarDTO dto) {
        try {
            operativoService.guardarSlot(empresaId, dto);
            return ResponseEntity.ok(Map.of("mensaje", "Slot guardado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/slots/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> toggleEstadoSlot(@PathVariable Long id) {
        try {
            operativoService.cambiarEstado(id);
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
}