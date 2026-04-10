package com.nodo.inv.controller;

import com.nodo.inv.dto.DocumentoDTO.CrearDocumentoRequest;
import com.nodo.inv.entity.Documento;
import com.nodo.inv.repository.DocumentoRepository;
import com.nodo.inv.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final DocumentoRepository documentoRepository;

    // ======================================================================
    // 1. CREACIÓN / LIQUIDACIÓN (El motor en acción)
    // ======================================================================
    
    @PostMapping("/liquidar")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> liquidarDocumento(@RequestBody CrearDocumentoRequest request) {
        log.info("Recibida petición de liquidación para la plantilla: {}", request.codigoLiquidacion());
        try {
            Documento nuevoDocumento = documentoService.generarDocumentoLiquidacion(request);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Documento generado exitosamente",
                "consecutivo", nuevoDocumento.getConsecutivo(),
                "total", nuevoDocumento.getTotalDocumento(),
                "idDocumento", nuevoDocumento.getId()
            ));
        } catch (Exception e) {
            log.error("Error al liquidar documento: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ======================================================================
    // 2. CONSULTAS (Para mostrar en las tablas de React)
    // ======================================================================

    @GetMapping("/empresa/{empresaId}/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Documento>> obtenerHistorialEmpresa(@PathVariable Long empresaId) {
        // Trae todas las facturas de la empresa ordenadas de la más nueva a la más vieja
        return ResponseEntity.ok(documentoRepository.findByEmpresaIdOrderByFechaEmisionDesc(empresaId));
    }

    @GetMapping("/empresa/{empresaId}/cartera")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Documento>> obtenerCarteraPendiente(@PathVariable Long empresaId) {
        // Trae solo las facturas que tienen un saldo mayor a 0 (Las que el cliente debe)
        return ResponseEntity.ok(documentoRepository.findDocumentosConSaldoPendiente(empresaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return documentoRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/reliquidar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> reliquidar(@PathVariable Long id) {
        try {
            Documento nuevoDoc = documentoService.reliquidarDocumento(id);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Reliquidación exitosa",
                "nuevoConsecutivo", nuevoDoc.getConsecutivo()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}