package com.nodo.inv.controller;

import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private SyncService syncService;
    
    @Autowired
    private com.nodo.inv.repository.ActividadOperativaRepository actividadRepo;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarActividad(@RequestBody SincronizacionPaqueteDTO paquete) {
        try {
            Map<String, Object> resultado = syncService.procesarPaquete(paquete);
            return ResponseEntity.ok(resultado);
        } catch (DataIntegrityViolationException e) {
            // 🔥 ESCUDO ANTI-CRASH: Si llegan dos peticiones idénticas al mismo milisegundo (Race Condition),
            // ignoramos el choque y le decimos a la tablet "OK" para que libere su memoria sin tumbar el servidor.
            return ResponseEntity.ok(Map.of("procesados", 0, "omitidos", paquete.getEventos().size(), "msg", "Duplicado paralelo omitido"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/empresa/{empresaId}/actividad")
    public ResponseEntity<?> obtenerActividadGlobal(@PathVariable Long empresaId) {
        List<com.nodo.inv.entity.ActividadOperativa> actividades = actividadRepo.findByEmpresaIdOrderByFechaDispositivoDesc(empresaId);
        
        List<java.util.Map<String, Object>> respuestaLimpia = actividades.stream().map(a -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("eventoId", a.getEventoId());
            map.put("tipoEvento", a.getTipoEvento());
            map.put("fechaDispositivo", a.getFechaDispositivo());
            map.put("detallesJson", a.getDetallesJson());
            if (a.getMesa() != null) {
                map.put("mesaId", a.getMesa().getIdMesaLocal());
            }
            return map;
        }).toList();

        return ResponseEntity.ok(respuestaLimpia);
    }
}