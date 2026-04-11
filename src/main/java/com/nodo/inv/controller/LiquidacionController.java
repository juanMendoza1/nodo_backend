package com.nodo.inv.controller;

import com.nodo.inv.dto.LiquidacionDTO;
import com.nodo.inv.entity.Liquidacion;
import com.nodo.inv.repository.LiquidacionRepository;
import com.nodo.inv.service.LiquidacionService;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config-liquidaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;
    private final LiquidacionRepository liquidacionRepository;

    /**
     * Solo tú (SUPER) creas las plantillas maestras del sistema.
     */
    @PostMapping("/plantilla")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<?> crearPlantilla(@RequestBody LiquidacionDTO dto) {
        try {
            return ResponseEntity.ok(liquidacionService.crearPlantillaGlobal(dto));
        } catch (Exception e) {
            // 🔥 AJUSTE: Ahora maneja el error si el código ya existe
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ADMIN o SUPER pueden amarrar conceptos a la plantilla.
     */
    @PostMapping("/empresa/{empresaId}/configurar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> configurarEmpresa(@PathVariable Long empresaId, @RequestBody LiquidacionDTO dto) {
        try {
            liquidacionService.configurarConceptos(empresaId, dto);
            return ResponseEntity.ok(Map.of("mensaje", "Configuración de conceptos guardada con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔥 LO QUE TE FALTABA: 
     * El Frontend llama esto para pintar la lista de conceptos ya guardados
     * y mostrarlos en el "Constructor de Recetas" para que el Admin los edite.
     */
    @GetMapping("/empresa/{empresaId}/plantilla/{codigoLiquidacion}/programa/{programaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerRecetaActual(
            @PathVariable Long empresaId,
            @PathVariable String codigoLiquidacion,
            @PathVariable Long programaId) {
        try {
            return ResponseEntity.ok(liquidacionService.obtenerConfiguracionActual(codigoLiquidacion, empresaId, programaId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/programa/{programaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Liquidacion>> listarPorPrograma(@PathVariable Long programaId) {
        if (programaId == 0) return ResponseEntity.ok(liquidacionRepository.findTransversales());
        return ResponseEntity.ok(liquidacionRepository.findByProgramaId(programaId));
    }
    
    @DeleteMapping("/plantilla/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> eliminarPlantilla(@PathVariable Long id) {
        try {
            liquidacionRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Liquidación eliminada correctamente"));
        } catch (DataIntegrityViolationException e) {
            // 🔥 Atrapamos el error SQL 23503 exacto de la llave foránea
            return ResponseEntity.badRequest().body(Map.of(
                "error", "⚠️ Acción bloqueada: No se puede eliminar esta liquidación porque ya tiene conceptos relacionados. Por favor, desvincúlelos primero."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Ocurrió un error inesperado al intentar eliminar."
            ));
        }
    }
}