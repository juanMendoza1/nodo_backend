package com.nodo.inv.controller;

import com.nodo.inv.entity.AcuerdoPagoSlot;
import com.nodo.inv.entity.LiquidacionSlot;
import com.nodo.inv.entity.NovedadSlot;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.entity.Venta;
import com.nodo.inv.repository.UsuarioRepository;
import com.nodo.inv.service.LiquidacionSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/liquidaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LiquidacionSlotController {

    private final LiquidacionSlotService liquidacionService;
    private final UsuarioRepository usuarioRepository;

    // ========================================================================
    // 1. GESTIÓN DE CONTRATOS (ACUERDOS DE PAGO)
    // ========================================================================
    
    @PostMapping("/acuerdo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> crearAcuerdo(@RequestBody AcuerdoPagoSlot acuerdo) {
        try {
            liquidacionService.crearAcuerdo(acuerdo);
            // Devolvemos un Map limpio para evitar el error del HttpMessageNotWritableException
            return ResponseEntity.ok(Map.of("mensaje", "Contrato generado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/acuerdo/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> finalizarAcuerdo(@PathVariable Long id) {
        try {
            liquidacionService.finalizarAcuerdo(id);
            return ResponseEntity.ok(Map.of("mensaje", "Contrato finalizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/acuerdo/{slotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerAcuerdoActivo(@PathVariable Long slotId) {
        try {
            AcuerdoPagoSlot acuerdo = liquidacionService.obtenerAcuerdoActivo(slotId);
            return ResponseEntity.ok(acuerdo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/acuerdos/historial/{slotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerHistorialAcuerdos(@PathVariable Long slotId) {
        try {
            return ResponseEntity.ok(liquidacionService.obtenerHistorialAcuerdos(slotId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 🔥 NUEVO: Endpoint para eliminar un contrato permanentemente
    @DeleteMapping("/acuerdo/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> eliminarAcuerdo(@PathVariable Long id) {
        try {
            liquidacionService.eliminarAcuerdo(id);
            return ResponseEntity.ok(Map.of("mensaje", "Contrato eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================================================
    // 2. GESTIÓN DE NOVEDADES Y VENTAS
    // ========================================================================

    @GetMapping("/ventas-resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerResumenVentas(
            @RequestParam Long slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            List<Venta> ventas = liquidacionService.consultarVentasRango(slotId, fechaInicio, fechaFin);
            
            BigDecimal total = ventas.stream()
                                     .map(Venta::getGranTotal)
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);
                                     
            return ResponseEntity.ok(Map.of(
                "cantidadVentas", ventas.size(),
                "totalRecaudado", total
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/novedad/acuerdo/{acuerdoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> registrarNovedad(@PathVariable Long acuerdoId, @RequestBody NovedadSlot novedad) {
        try {
            return ResponseEntity.ok(liquidacionService.registrarNovedad(novedad, acuerdoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================================================
    // 3. MOTOR DE LIQUIDACIÓN
    // ========================================================================

    @GetMapping("/previsualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> previsualizarLiquidacion(
            @RequestParam Long empresaId,
            @RequestParam Long slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            LiquidacionSlot borrador = liquidacionService.calcularLiquidacion(empresaId, slotId, fechaInicio, fechaFin);
            return ResponseEntity.ok(borrador);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> generarPagoDefinitivo(
            @RequestParam Long empresaId,
            @RequestParam Long slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam Long adminId) {
        try {
            Usuario admin = usuarioRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
            LiquidacionSlot oficial = liquidacionService.generarPagoDefinitivo(empresaId, slotId, fechaInicio, fechaFin, admin);
            return ResponseEntity.ok(Map.of("mensaje", "Liquidación generada", "data", oficial));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/acuerdo/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> actualizarAcuerdo(@PathVariable Long id, @RequestBody AcuerdoPagoSlot acuerdo) {
        try {
            // Devolvemos el acuerdo actualizado para que React refresque la vista
            return ResponseEntity.ok(liquidacionService.actualizarAcuerdo(id, acuerdo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/novedades/pendientes/acuerdo/{acuerdoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerNovedadesPendientes(@PathVariable Long acuerdoId) {
        try {
            return ResponseEntity.ok(liquidacionService.obtenerNovedadesPendientes(acuerdoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/pagos/historial/{slotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerHistorialPagos(@PathVariable Long slotId) {
        try {
            return ResponseEntity.ok(liquidacionService.obtenerHistorialPagosSlot(slotId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}