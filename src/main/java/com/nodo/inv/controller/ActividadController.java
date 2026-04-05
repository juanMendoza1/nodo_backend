package com.nodo.inv.controller;

import com.nodo.inv.entity.ActividadOperativa;
import com.nodo.inv.repository.ActividadOperativaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/actividad")
@CrossOrigin(origins = "*")
public class ActividadController {

    @Autowired
    private ActividadOperativaRepository actividadRepo;

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<?> obtenerActividadGlobal(@PathVariable Long empresaId) {
        
        List<ActividadOperativa> actividades = actividadRepo.findByEmpresaIdOrderByFechaDispositivoDesc(empresaId);
        
        List<Map<String, Object>> respuestaLimpia = actividades.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("eventoId", a.getEventoId());
            map.put("tipoEvento", a.getTipoEvento());
            map.put("fechaDispositivo", a.getFechaDispositivo());
            map.put("detallesJson", a.getDetallesJson());
            
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