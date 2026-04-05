package com.nodo.inv.controller;

import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.service.SyncService;
import com.nodo.inv.repository.ActividadOperativaRepository;
import com.nodo.inv.entity.ActividadOperativa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private SyncService syncService;
    
    @Autowired
    private ActividadOperativaRepository actividadRepo;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarActividad(@RequestBody SincronizacionPaqueteDTO paquete) {
        try {
            Map<String, Object> resultado = syncService.procesarPaquete(paquete);
            return ResponseEntity.ok(resultado);
        } catch (DataIntegrityViolationException e) {
            // 🔥 ESCUDO ANTI-DUPLICADOS: Responde OK para que la tablet limpie su cola
            return ResponseEntity.ok(Map.of(
                "status", "ignored",
                "message", "Evento duplicado omitido correctamente",
                "omitidos", paquete.getEventos().size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/empresa/{empresaId}/actividad")
    public ResponseEntity<?> obtenerActividadGlobal(@PathVariable Long empresaId) {
        // Traemos el historial ordenado por fecha descendente
        List<ActividadOperativa> actividades = actividadRepo.findByEmpresaIdOrderByFechaDispositivoDesc(empresaId);
        
        // Mapeamos a una respuesta limpia para evitar errores de nesting/profundidad JSON
        List<Map<String, Object>> respuestaLimpia = actividades.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("eventoId", a.getEventoId());
            map.put("tipoEvento", a.getTipoEvento());
            map.put("fechaDispositivo", a.getFechaDispositivo());
            map.put("detallesJson", a.getDetallesJson());
            
            // 🔥 PASAMOS EL ID DE LA MESA PARA LA COLUMNA "UBICACIÓN"
            if (a.getMesa() != null) {
                map.put("mesaId", a.getMesa().getIdMesaLocal());
            } else {
                map.put("mesaId", null);
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(respuestaLimpia);
    }
}