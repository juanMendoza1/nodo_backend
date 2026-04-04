package com.nodo.inv.controller;

import com.nodo.inv.dto.MesaDTO;
import com.nodo.inv.service.MesaService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MesaController {

    private final MesaService mesaService;
    
    @Autowired
    private com.nodo.inv.repository.ActividadOperativaRepository actividadRepo;

    /**
     * Endpoint que la Tablet llamará al iniciar sesión para reconstruir su salón.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<List<MesaDTO>> listarEstadoMesas(@PathVariable Long empresaId) {
        return ResponseEntity.ok(mesaService.obtenerEstadoMesasPorEmpresa(empresaId));
    }
    
    @GetMapping("/empresa/{empresaId}/mesa/{idMesaLocal}/actividad")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerActividadDeMesa(@PathVariable Long empresaId, @PathVariable Integer idMesaLocal) {
        // Retorna todos los eventos crudos de la mesa, el Frontend (React) se encargará de armar la cuenta.
        return ResponseEntity.ok(actividadRepo.findByEmpresaAndMesaLocal(empresaId, idMesaLocal));
    }
    
}