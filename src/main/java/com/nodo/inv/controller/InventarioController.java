package com.nodo.inv.controller;

import com.nodo.inv.entity.InventarioMovimiento;
import com.nodo.inv.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    /**
     * Registra un movimiento de inventario general (Entradas por compra, ajustes, etc.)
     * Solo accesible para usuarios con rol ADMIN o SUPER.
     */
    @PostMapping("/movimiento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> registrar(@RequestBody InventarioMovimiento movimiento) {
        try {
            return ResponseEntity.ok(inventarioService.registrarMovimiento(movimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint especializado para la App Móvil.
     * Procesa el despacho de productos (Estado 'ENTREGADO' en la tablet).
     * Resta automáticamente el stock y genera el rastro con la referencia del duelo.
     */
    
    @PostMapping("/despacho-mesa")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> procesarDespacho(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam String idDuelo,
            @RequestParam String loginOperativo) {
    	try {
            inventarioService.procesarDespachoDesdeApp(productoId, cantidad, idDuelo, loginOperativo);
            // Enviamos un Mapa que Spring convertirá automáticamente a JSON
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Despacho procesado",
                "productoId", productoId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene el historial de movimientos de una empresa específica.
     */
    @GetMapping("/historial/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<InventarioMovimiento>> listarHistorial(@PathVariable Long empresaId) {
        return ResponseEntity.ok(inventarioService.obtenerHistorialPorEmpresa(empresaId));
    }
}