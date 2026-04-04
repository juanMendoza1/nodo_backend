package com.nodo.inv.controller;

import com.nodo.inv.dto.MesaDTO;
import com.nodo.inv.service.MesaService;
import lombok.RequiredArgsConstructor;
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

    /**
     * Endpoint que la Tablet llamará al iniciar sesión para reconstruir su salón.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<List<MesaDTO>> listarEstadoMesas(@PathVariable Long empresaId) {
        return ResponseEntity.ok(mesaService.obtenerEstadoMesasPorEmpresa(empresaId));
    }
}