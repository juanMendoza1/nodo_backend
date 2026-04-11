package com.nodo.inv.controller;

import com.nodo.inv.entity.TipoDocumento;
import com.nodo.inv.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-documentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TipoDocumentoController {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<TipoDocumento>> listarTodos() {
        // Retornamos todos los documentos activos (FV, CE, RC, etc.)
        return ResponseEntity.ok(tipoDocumentoRepository.findAll());
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<TipoDocumento> obtenerPorCodigo(@PathVariable String codigo) {
        return tipoDocumentoRepository.findByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}