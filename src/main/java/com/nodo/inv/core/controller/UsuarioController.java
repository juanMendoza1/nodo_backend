package com.nodo.inv.core.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.entity.UsuarioOperativo;
// Importamos el nuevo DTO para el CRUD del Panel Web
import com.nodo.inv.dto.UsuarioDataDTO; 

import com.nodo.inv.dto.UsuarioRegistroDTO; // Por si lo tienes en uso en AuthController
import com.nodo.inv.dto.UsuarioSlotDTO;
import com.nodo.inv.dto.UsuarioSlotGuardarDTO;
import com.nodo.inv.service.UsuarioOperativoService;
import com.nodo.inv.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioOperativoService operativoService;

    // ======================================================================
    // 1. NUEVO CRUD DE USUARIOS DEL SISTEMA (PANEL SUPER ADMIN WEB)
    // ======================================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> crear(@RequestBody UsuarioDataDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.guardarUsuario(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody UsuarioDataDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(usuarioService.guardarUsuario(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')") // Solo el SuperAdmin debería poder borrar
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede eliminar el usuario porque ya tiene historial operativo."));
        }
    }

    // ======================================================================
    // 2. GESTIÓN DE USUARIOS OPERATIVOS (SLOTS PARA TABLET / ANDROID)
    // ======================================================================

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