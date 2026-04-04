package com.nodo.inv.controller;

import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
@CrossOrigin(origins = "*") // O configúralo según tus políticas de CORS
public class SyncController {

    @Autowired
    private SyncService syncService;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarActividad(@RequestBody SincronizacionPaqueteDTO paquete) {
        // Le pasamos el paquete al servicio para que haga el trabajo duro
        Map<String, Object> resultado = syncService.procesarPaquete(paquete);
        
        // Respondemos 200 OK inmediatamente para que la tablet libere su memoria
        return ResponseEntity.ok(resultado);
    }
}