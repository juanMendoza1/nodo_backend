package com.nodo.inv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.DocumentoDTO.CrearDocumentoRequest;
import com.nodo.inv.dto.DocumentoDTO.LineaDetalle;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import com.nodo.inv.service.engine.LiquidacionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final LiquidacionEngine liquidacionEngine;
    private final DocumentoRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ConceptoLiquidacionRepository recetaRepository;
    private final ConceptoRepository conceptoRepository;
    private final EmpresaRepository empresaRepository;
    private final ProgramaRepository programaRepository;
    private final TerceroRepository terceroRepository;
    private final ConsecutivoDocumentoRepository consecutivoRepository;
    private final ObjectMapper objectMapper;

    // ========================================================================
    // 1. PRE-LIQUIDACIÓN SÍNCRONA (BORRADOR EN MEMORIA RAM)
    // ========================================================================
    @Transactional(readOnly = true)
    public Map<String, Object> preliquidarDocumento(CrearDocumentoRequest request) {
        log.info("Generando Proforma (Memoria) para Empresa ID: {}", request.empresaId());

        Empresa empresa = empresaRepository.findById(request.empresaId()).orElseThrow();
        
        // 🔥 LÓGICA TRANSVERSAL: Si el programa es 0, no lo buscamos (es Global/SaaS)
        Programa programa = null;
        Long progIdBuscador = 0L;
        if (request.programaId() != null && request.programaId() != 0) {
            programa = programaRepository.findById(request.programaId()).orElseThrow();
            progIdBuscador = programa.getId();
        }

        // 1. Buscar la Receta
        List<ConceptoLiquidacion> receta = recetaRepository.obtenerRecetaDeLiquidacion(
                request.codigoLiquidacion(), empresa.getId(), progIdBuscador);

        if (receta.isEmpty()) {
            throw new RuntimeException("No hay una matriz matemática configurada para: " + request.codigoLiquidacion());
        }

        // 2. Ejecutar Motor Matemático en Memoria
        List<LineaDetalle> lineasCalculadas = liquidacionEngine.ejecutarLiquidacion(receta, request.valoresOperativos());

        // 3. Armar el JSON de respuesta (El "Ticket" del Frontend)
        BigDecimal granTotal = BigDecimal.ZERO;
        List<Map<String, Object>> detallesProforma = new ArrayList<>();

        for (LineaDetalle lineaDTO : lineasCalculadas) {
            Concepto concepto = conceptoRepository.findByCodigo(lineaDTO.conceptoCodigo()).orElseThrow();
            
            // Si el nombre dice "Descuento", asumimos visualmente que resta, sino suma.
            // (Nota: El motor ya devuelve el valor neto, esto es solo para pintar el UI)
            String naturalezaVisual = concepto.getNombre().toLowerCase().contains("descuento") ? "RESTA" : "SUMA";

            Map<String, Object> detalle = new HashMap<>();
            detalle.put("conceptoCodigo", concepto.getCodigo());
            detalle.put("conceptoNombre", concepto.getNombre());
            detalle.put("naturaleza", naturalezaVisual);
            detalle.put("valorTotal", lineaDTO.valorTotal());
            
            detallesProforma.add(detalle);
            
            // Sumamos o restamos del Gran Total según la naturaleza
            if (naturalezaVisual.equals("RESTA")) {
                granTotal = granTotal.subtract(lineaDTO.valorTotal());
            } else {
                granTotal = granTotal.add(lineaDTO.valorTotal());
            }
        }

        // 4. Retornar el Mapa empaquetado para React (Sin tocar la Base de Datos)
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("total", granTotal);
        respuesta.put("detalles", detallesProforma);
        
        // Extraemos las métricas operativas (Ej: CANTIDAD_TABLETS) para el monitor
        Map<String, Object> stats = new HashMap<>();
        stats.put("dispositivosActivos", request.valoresOperativos().getOrDefault("CANTIDAD_TABLETS", BigDecimal.ZERO));
        respuesta.put("statsOperativas", stats);

        return respuesta;
    }


    // ========================================================================
    // 2. LIQUIDACIÓN OFICIAL Y CIERRE FINANCIERO (CONSECUTIVO BLOQUEADO)
    // ========================================================================
    @Transactional
    public Documento generarDocumentoLiquidacion(CrearDocumentoRequest request) {
        log.info("Sellando liquidación en Base de Datos para Empresa ID: {}", request.empresaId());

        Empresa empresa = empresaRepository.findById(request.empresaId()).orElseThrow();
        Tercero tercero = terceroRepository.findById(request.terceroId()).orElseThrow();
        TipoDocumento tipoDoc = tipoDocumentoRepository.findByCodigo(request.tipoDocumentoCodigo()).orElseThrow();

        // 🔥 LÓGICA TRANSVERSAL
        Programa programa = null;
        Long progIdBuscador = 0L;
        if (request.programaId() != null && request.programaId() != 0) {
            programa = programaRepository.findById(request.programaId()).orElseThrow();
            progIdBuscador = programa.getId();
        }

        List<ConceptoLiquidacion> receta = recetaRepository.obtenerRecetaDeLiquidacion(
                request.codigoLiquidacion(), empresa.getId(), progIdBuscador);

        if (receta.isEmpty()) {
            throw new RuntimeException("No hay una matriz configurada para: " + request.codigoLiquidacion());
        }

        // Llamar al motor matemático
        List<LineaDetalle> lineasCalculadas = liquidacionEngine.ejecutarLiquidacion(receta, request.valoresOperativos());

        // Crear la cabecera contable
        Documento nuevoDocumento = new Documento();
        nuevoDocumento.setEmpresa(empresa);
        nuevoDocumento.setPrograma(programa); // Será null si es B2B Transversal
        nuevoDocumento.setTercero(tercero);
        nuevoDocumento.setTipoDocumento(tipoDoc);
        nuevoDocumento.setFechaEmision(LocalDateTime.now());
        nuevoDocumento.setEstado("EMITIDO");
        
        // Consecutivo Seguro (Pesimista)
        nuevoDocumento.setConsecutivo(generarConsecutivoSeguro(empresa, tipoDoc));

        try {
            nuevoDocumento.setMetadataJson(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("No se pudo serializar la petición original", e);
        }

        BigDecimal granTotal = BigDecimal.ZERO;
        BigDecimal granSaldo = BigDecimal.ZERO;

        for (LineaDetalle lineaDTO : lineasCalculadas) {
            Concepto concepto = conceptoRepository.findByCodigo(lineaDTO.conceptoCodigo()).orElseThrow();

            DocumentoDetalle detalle = new DocumentoDetalle();
            detalle.setConcepto(concepto);
            detalle.setCantidad(lineaDTO.cantidad());
            
            // Evitar división por cero
            BigDecimal cant = lineaDTO.cantidad().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : lineaDTO.cantidad();
            detalle.setValorUnitario(lineaDTO.valorTotal().divide(cant, 2, java.math.RoundingMode.HALF_UP)); 
            
            detalle.setValorTotal(lineaDTO.valorTotal());
            detalle.setValorReal(lineaDTO.saldo()); 
            detalle.setSaldo(lineaDTO.saldo());
            
            // Asignación de Naturaleza Contable
            String naturaleza = concepto.getNombre().toLowerCase().contains("descuento") ? "RESTA" : "SUMA";
            detalle.setNaturaleza(naturaleza);

            nuevoDocumento.addDetalle(detalle);
            
            if (naturaleza.equals("RESTA")) {
                granTotal = granTotal.subtract(detalle.getValorTotal());
                granSaldo = granSaldo.subtract(detalle.getSaldo());
            } else {
                granTotal = granTotal.add(detalle.getValorTotal());
                granSaldo = granSaldo.add(detalle.getSaldo());
            }
        }

        nuevoDocumento.setTotalDocumento(granTotal);
        nuevoDocumento.setSaldoDocumento(granSaldo);

        return documentoRepository.save(nuevoDocumento);
    }

    // ... (Mantén el resto del código igual: reliquidarDocumento, generarConsecutivoSeguro, etc.)
    
    @Transactional
    public Documento reliquidarDocumento(Long idDocumentoOriginal) {
        Documento original = documentoRepository.findById(idDocumentoOriginal)
                .orElseThrow(() -> new RuntimeException("Documento original no encontrado"));

        if ("ANULADO".equals(original.getEstado())) {
            throw new RuntimeException("El documento ya se encuentra anulado. No se puede reliquidar de nuevo.");
        }

        CrearDocumentoRequest peticionOriginal;
        try {
            peticionOriginal = objectMapper.readValue(original.getMetadataJson(), CrearDocumentoRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer la metadata original para realizar la reliquidación.");
        }

        original.setEstado("ANULADO");
        original.setObservaciones("ANULADO POR RELIQUIDACIÓN. Saldos en cero.");
        original.setSaldoDocumento(BigDecimal.ZERO);
        original.getDetalles().forEach(det -> det.setSaldo(BigDecimal.ZERO));
        
        documentoRepository.save(original);

        Documento nuevoDocumento = generarDocumentoLiquidacion(peticionOriginal);
        nuevoDocumento.setDocumentoPadre(original);
        nuevoDocumento.setObservaciones("RELIQUIDACIÓN DEL DOCUMENTO: " + original.getConsecutivo());
        
        return documentoRepository.save(nuevoDocumento);
    }

    private String generarConsecutivoSeguro(Empresa empresa, TipoDocumento tipoDoc) {
        ConsecutivoDocumento consecutivo = consecutivoRepository
                .findByEmpresaAndTipoDocumentoForUpdate(empresa.getId(), tipoDoc.getId())
                .orElseGet(() -> {
                    ConsecutivoDocumento nuevo = new ConsecutivoDocumento();
                    nuevo.setEmpresa(empresa);
                    nuevo.setTipoDocumento(tipoDoc);
                    nuevo.setPrefijo(tipoDoc.getCodigo()); 
                    nuevo.setActual(0L);
                    return consecutivoRepository.save(nuevo);
                });

        consecutivo.setActual(consecutivo.getActual() + 1);
        consecutivoRepository.save(consecutivo);
        return consecutivo.getPrefijo() + "-" + String.format("%06d", consecutivo.getActual());
    }
}