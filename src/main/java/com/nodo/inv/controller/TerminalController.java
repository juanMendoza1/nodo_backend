package com.nodo.inv.controller;

import com.nodo.inv.entity.TerminalDispositivo;
import com.nodo.inv.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terminales")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;

    @PostMapping("/activar")
    public ResponseEntity<?> registrarTerminal(
            @RequestParam Long empresaId,
            @RequestParam String programaCod,
            @RequestBody TerminalDispositivo datosTablet) {
        try {
            return ResponseEntity.ok(terminalService.activarTerminal(empresaId, programaCod, datosTablet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validar/{uuid}")
    public ResponseEntity<?> validar(@PathVariable String uuid) {
        try {
            terminalService.validarAccesoTerminal(uuid);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}