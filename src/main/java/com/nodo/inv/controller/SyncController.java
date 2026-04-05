package com.nodo.inv.controller;

import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    @Autowired
    private SyncService syncService;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarActividad(@RequestBody SincronizacionPaqueteDTO paquete) {
        try {
            Map<String, Object> resultado = syncService.procesarPaquete(paquete);
            return ResponseEntity.ok(resultado);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.ok(Map.of(
                "status", "ignored",
                "message", "Evento duplicado omitido correctamente",
                "omitidos", paquete.getEventos().size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}