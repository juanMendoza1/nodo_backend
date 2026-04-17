package com.nodo.inv.core.controller;

import com.nodo.inv.core.dto.DocumentoDTO;
import com.nodo.inv.core.dto.DocumentoDTO.CrearDocumentoRequest;
import com.nodo.inv.core.entity.Documento;
import com.nodo.inv.core.repository.DocumentoRepository;
import com.nodo.inv.core.service.DocumentoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/preliquidar")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> preliquidarDocumento(@RequestBody CrearDocumentoRequest request) {
        try {
            return ResponseEntity.ok(documentoService.preliquidarDocumento(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/liquidar")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> liquidarDocumento(@RequestBody CrearDocumentoRequest request) {
        try {
            Documento nuevoDocumento = documentoService.generarDocumentoLiquidacion(request);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Documento generado exitosamente",
                "consecutivo", nuevoDocumento.getConsecutivo(),
                "total", nuevoDocumento.getTotalDocumento(),
                "idDocumento", nuevoDocumento.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ======================================================================
    // 🔥 NUEVO: LIQUIDACIÓN MASIVA (BATCH)
    // ======================================================================
    @PostMapping("/liquidar-lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> liquidarPorLote(@RequestBody DocumentoDTO.LiquidarLoteRequest request) {
        log.info("Recibida petición masiva para el Ciclo: {}", request.cicloId());
        try {
            Map<String, Object> resultado = documentoService.liquidarLotePorCiclo(request);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error en liquidación masiva: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ======================================================================
    // 3. CONSULTAS Y RELIQUIDACIÓN (Intacto)
    // ======================================================================
    @GetMapping("/empresa/{empresaId}/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<Page<Documento>> obtenerHistorialEmpresa(@PathVariable Long empresaId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(documentoRepository.findByEmpresaIdOrderByFechaEmisionDesc(empresaId, pageable));
    }

    @GetMapping("/empresa/{empresaId}/cartera")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<Documento>> obtenerCarteraPendiente(@PathVariable Long empresaId) {
        return ResponseEntity.ok(documentoRepository.findDocumentosConSaldoPendiente(empresaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATIVO', 'ADMIN', 'SUPER')")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return documentoRepository.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/reliquidar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> reliquidar(@PathVariable Long id) {
        try {
            Documento nuevoDoc = documentoService.reliquidarDocumento(id);
            return ResponseEntity.ok(Map.of("mensaje", "Reliquidación exitosa", "nuevoConsecutivo", nuevoDoc.getConsecutivo()));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
    
    @GetMapping("/empresa/{empresaId}/buscar-padre")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> buscarPadrePorConsecutivo(@PathVariable Long empresaId, @RequestParam String consecutivo) {
        try { return ResponseEntity.ok(documentoService.buscarPadrePorConsecutivo(empresaId, consecutivo)); } 
        catch (Exception e) { return ResponseEntity.status(404).body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/emitir-nota")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<?> emitirNota(@RequestBody DocumentoDTO.EmitirNotaRequest request) {
        try {
            Documento nota = documentoService.emitirNota(request);
            return ResponseEntity.ok(Map.of("mensaje", "Nota emitida exitosamente", "consecutivo", nota.getConsecutivo(), "total", nota.getTotalDocumento()));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
    
    @PutMapping("/lote/aprobar")
    public ResponseEntity<?> aprobarLote(@RequestParam Long cicloId, @RequestParam Long periodoId) {
        return ResponseEntity.ok(documentoService.aprobarLote(cicloId, periodoId));
    }
}