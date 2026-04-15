package com.nodo.inv.core.controller;

import com.nodo.inv.core.dto.CicloFacturacionDTO;
import com.nodo.inv.core.entity.CicloFacturacion;
import com.nodo.inv.core.entity.PeriodoFacturacion;
import com.nodo.inv.core.service.CicloFacturacionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ciclos-facturacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CicloFacturacionController {

    private final CicloFacturacionService cicloService;

    // --- CRUD DEL CICLO ---
    @GetMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<List<CicloFacturacion>> listarCiclos() {
        return ResponseEntity.ok(cicloService.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crearCiclo(@RequestBody CicloFacturacionDTO dto) {
        try {
            // Por defecto proyectamos el año actual
            int anioActual = LocalDate.now().getYear();
            return ResponseEntity.ok(cicloService.guardarCiclo(dto, anioActual));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> actualizarCiclo(@PathVariable Long id, @RequestBody CicloFacturacionDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(cicloService.guardarCiclo(dto, LocalDate.now().getYear()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- GESTIÓN DE PERIODOS ---
    @GetMapping("/{cicloId}/periodos")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<List<PeriodoFacturacion>> listarPeriodos(@PathVariable Long cicloId) {
        return ResponseEntity.ok(cicloService.obtenerPeriodosPorCiclo(cicloId));
    }

    @PostMapping("/{cicloId}/proyectar/{anio}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> proyectarAnio(@PathVariable Long cicloId, @PathVariable int anio) {
        try {
            CicloFacturacion ciclo = new CicloFacturacion(); // Mock para pasar el ID
            ciclo.setId(cicloId);
            // Idealmente deberías buscar el ciclo en la BD aquí antes de pasarlo
            cicloService.generarProyeccionAnual(ciclo, anio);
            return ResponseEntity.ok(Map.of("mensaje", "Año " + anio + " proyectado con éxito."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- MÁQUINA DE ESTADOS ---
    @PutMapping("/periodos/{periodoId}/abrir")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> abrirPeriodo(@PathVariable Long periodoId) {
        try {
            cicloService.abrirPeriodo(periodoId);
            return ResponseEntity.ok(Map.of("mensaje", "Periodo ABIERTO correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/periodos/{periodoId}/procesar")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> procesarPeriodo(@PathVariable Long periodoId) {
        try {
            cicloService.iniciarProcesoLiquidacion(periodoId);
            return ResponseEntity.ok(Map.of("mensaje", "Periodo en estado LIQUIDANDO. Motor bloqueado para emisión de facturas."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/periodos/{periodoId}/cerrar")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> cerrarPeriodo(@PathVariable Long periodoId) {
        try {
            cicloService.cerrarPeriodo(periodoId);
            return ResponseEntity.ok(Map.of("mensaje", "Periodo CERRADO exitosamente. Facturación sellada."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}