package com.nodo.inv.controller;

import com.nodo.inv.dto.TerminalCupoDTO;
import com.nodo.inv.entity.TerminalDispositivo;
import com.nodo.inv.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/terminales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TerminalController {

    private final TerminalService terminalService;

    // --- ENDPOINTS ORIGINALES ---

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

    // --- NUEVOS ENDPOINTS PARA GESTIÓN DE QR Y CUPOS (WEB ADMIN) ---

    /**
     * Consulta el estado de cupos (máximos vs activos) para mostrar en la interfaz web.
     * Solo accesible para roles ADMIN o SUPER.
     */
    @GetMapping("/cupos-disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerCupos(
            @RequestParam Long empresaId, 
            @RequestParam String programaCod) {
        try {
            TerminalCupoDTO cupos = terminalService.consultarCuposDisponibles(empresaId, programaCod);
            return ResponseEntity.ok(cupos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Genera un token único y temporal que la plataforma web convertirá en un código QR.
     * Solo accesible para roles ADMIN o SUPER.
     */
    @PostMapping("/generar-qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> crearToken(@RequestBody Map<String, Object> payload) {
        try {
            // Extraer parámetros del cuerpo de la petición
            Long empresaId = Long.valueOf(payload.get("empresaId").toString());
            String programaCod = payload.get("programaCod").toString();
            
            String token = terminalService.generarTokenRegistro(empresaId, programaCod);
            
            // Retornamos un JSON con el token para que el frontend genere el QR
            return ResponseEntity.ok(Map.of(
                "tokenRegistro", token,
                "mensaje", "Token generado con éxito. Válido por 10 minutos."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar token: " + e.getMessage());
        }
    }
    
    @PostMapping("/vincular-qr")
    public ResponseEntity<?> vincular(@RequestBody Map<String, Object> payload) {
        try {
            // Validación de seguridad antes de usar .toString()
            if (payload == null || !payload.containsKey("token") || payload.get("token") == null) {
                return ResponseEntity.badRequest().body("Error: El token es obligatorio");
            }

            String token = payload.get("token").toString();
            
            // Lo mismo para el uuidHardware
            if (payload.get("uuidHardware") == null) {
                return ResponseEntity.badRequest().body("Error: UUID de hardware no recibido");
            }

            TerminalDispositivo dispositivo = new TerminalDispositivo();
            dispositivo.setUuidHardware(payload.get("uuidHardware").toString());
            
            // Para alias, marca y modelo usa un valor por defecto si son nulos
            dispositivo.setAlias(payload.getOrDefault("alias", "Terminal Desconocida").toString());
            dispositivo.setMarca(payload.getOrDefault("marca", "Genérica").toString());
            dispositivo.setModelo(payload.getOrDefault("modelo", "Tablet").toString());

            TerminalDispositivo vinculado = terminalService.vincularPorQr(token, dispositivo);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Dispositivo vinculado con éxito",
                "empresaId", vinculado.getSuscripcion().getEmpresa().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}