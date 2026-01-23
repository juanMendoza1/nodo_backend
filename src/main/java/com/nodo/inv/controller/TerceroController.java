package com.nodo.inv.controller;

import com.nodo.inv.entity.Tercero;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.service.TerceroService;
import com.nodo.inv.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceros")
@RequiredArgsConstructor
public class TerceroController {

    private final TerceroService terceroService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crear un tercero vinculado a una empresa o global.
     * @param tercero Datos del tercero
     * @param empresaId ID de la empresa que lo registra (ej: ID de Billares Diego)
     * @param usuarioId ID del usuario que realiza la acción (ej: ID de Diego)
     * @param esGlobal true si lo crea Juan para todos, false si lo crea Diego para su inventario
     */
    @PostMapping
    public ResponseEntity<Tercero> crear(
            @RequestBody Tercero tercero,
            @RequestParam Long empresaId,
            @RequestParam Long usuarioId,
            @RequestParam boolean esGlobal) {
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        return ResponseEntity.ok(terceroService.crearTercero(tercero, empresaId, usuario, esGlobal));
    }

    /**
     * Lista solo los terceros que la empresa tiene permiso de ver
     * (Sus proveedores propios + los globales del sistema)
     */
    @GetMapping("/visibles/{empresaId}")
    public ResponseEntity<List<Tercero>> listarVisibles(@PathVariable Long empresaId) {
        return ResponseEntity.ok(terceroService.listarTercerosVisibles(empresaId));
    }

    /**
     * Lista absolutamente todos (Uso exclusivo para el Super Admin Juan)
     */
    @GetMapping("/admin/todos")
    public ResponseEntity<List<Tercero>> listarTodos() {
        return ResponseEntity.ok(terceroService.listarTodos());
    }
}