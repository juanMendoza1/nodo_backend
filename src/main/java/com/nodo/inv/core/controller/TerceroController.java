package com.nodo.inv.core.controller;

import com.nodo.inv.core.dto.TerceroDTO;
import com.nodo.inv.core.entity.Tercero;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.repository.UsuarioRepository;
import com.nodo.inv.core.service.TerceroService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terceros")
@RequiredArgsConstructor
public class TerceroController {

    private final TerceroService terceroService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody TerceroDTO dto, // 🔥 Ahora recibimos el DTO
            @RequestParam Long empresaId,
            @RequestParam Long usuarioId,
            @RequestParam boolean esGlobal) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    
            return ResponseEntity.ok(terceroService.crearTercero(dto, empresaId, usuario, esGlobal));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody TerceroDTO dto) {
        try {
            return ResponseEntity.ok(terceroService.actualizarTercero(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            terceroService.eliminarTercero(id);
            return ResponseEntity.ok(Map.of("message", "Registro eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/visibles/{empresaId}")
    public ResponseEntity<List<Tercero>> listarVisibles(@PathVariable Long empresaId) {
        return ResponseEntity.ok(terceroService.listarTercerosVisibles(empresaId));
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<Tercero>> listarTodos() {
        return ResponseEntity.ok(terceroService.listarTodos());
    }
}