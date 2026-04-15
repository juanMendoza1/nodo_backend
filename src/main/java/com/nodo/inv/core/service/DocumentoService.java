package com.nodo.inv.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.Utils.EstadoPeriodo;
import com.nodo.inv.core.dto.DocumentoDTO;
import com.nodo.inv.core.dto.DocumentoDTO.CrearDocumentoRequest;
import com.nodo.inv.core.dto.DocumentoDTO.LineaDetalle;
import com.nodo.inv.core.engine.LiquidacionEngine;
import com.nodo.inv.core.entity.*;
import com.nodo.inv.core.repository.*;

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
    
    // 🔥 NUEVOS REPOSITORIOS INYECTADOS
    private final SuscripcionProgramaRepository suscripcionRepo;
    private final CicloFacturacionRepository cicloRepository;
    private final PeriodoFacturacionRepository periodoRepository;

    // ========================================================================
    // 1. PRE-LIQUIDACIÓN (Igual)
    // ========================================================================
    @Transactional(readOnly = true)
    public Map<String, Object> preliquidarDocumento(CrearDocumentoRequest request) {
        Empresa empresa = empresaRepository.findById(request.empresaId()).orElseThrow();
        Programa programa = null;
        Long progIdBuscador = 0L;
        if (request.programaId() != null && request.programaId() != 0) {
            programa = programaRepository.findById(request.programaId()).orElseThrow();
            progIdBuscador = programa.getId();
        }

        List<ConceptoLiquidacion> receta = recetaRepository.obtenerRecetaDeLiquidacion(request.codigoLiquidacion(), empresa.getId(), progIdBuscador);
        if (receta.isEmpty()) throw new RuntimeException("No hay una matriz configurada.");

        List<LineaDetalle> lineasCalculadas = liquidacionEngine.ejecutarLiquidacion(receta, request.valoresOperativos());

        BigDecimal granTotal = BigDecimal.ZERO;
        List<Map<String, Object>> detallesProforma = new ArrayList<>();

        for (LineaDetalle lineaDTO : lineasCalculadas) {
            Map<String, Object> detalle = new HashMap<>();
            detalle.put("conceptoCodigo", lineaDTO.conceptoCodigo());
            detalle.put("conceptoNombre", lineaDTO.conceptoNombre());
            detalle.put("naturaleza", lineaDTO.naturaleza());
            detalle.put("valorTotal", lineaDTO.valorTotal());
            detallesProforma.add(detalle);
            
            if ("RESTA".equals(lineaDTO.naturaleza())) {
                granTotal = granTotal.subtract(lineaDTO.saldo());
            } else {
                granTotal = granTotal.add(lineaDTO.saldo());
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("total", granTotal);
        respuesta.put("detalles", detallesProforma);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("dispositivosActivos", request.valoresOperativos().getOrDefault("CANTIDAD_TABLETS", BigDecimal.ZERO));
        respuesta.put("statsOperativas", stats);

        return respuesta;
    }

    // ========================================================================
    // 2. LIQUIDACIÓN OFICIAL (UNO A UNO)
    // ========================================================================
    @Transactional
    public Documento generarDocumentoLiquidacion(CrearDocumentoRequest request) {
        Empresa empresa = empresaRepository.findById(request.empresaId()).orElseThrow();
        Tercero tercero = terceroRepository.findById(request.terceroId()).orElseThrow();
        TipoDocumento tipoDoc = tipoDocumentoRepository.findByCodigo(request.tipoDocumentoCodigo()).orElseThrow();

        Programa programa = null;
        Long progIdBuscador = 0L;
        if (request.programaId() != null && request.programaId() != 0) {
            programa = programaRepository.findById(request.programaId()).orElseThrow();
            progIdBuscador = programa.getId();
        }

        List<ConceptoLiquidacion> receta = recetaRepository.obtenerRecetaDeLiquidacion(request.codigoLiquidacion(), empresa.getId(), progIdBuscador);
        if (receta.isEmpty()) throw new RuntimeException("No hay matriz configurada.");

        List<LineaDetalle> lineasCalculadas = liquidacionEngine.ejecutarLiquidacion(receta, request.valoresOperativos());

        Documento nuevoDocumento = new Documento();
        nuevoDocumento.setEmpresa(empresa);
        nuevoDocumento.setPrograma(programa); 
        nuevoDocumento.setTercero(tercero);
        nuevoDocumento.setTipoDocumento(tipoDoc);
        nuevoDocumento.setFechaEmision(LocalDateTime.now());
        nuevoDocumento.setEstado("EMITIDO");
        nuevoDocumento.setConsecutivo(generarConsecutivoSeguro(empresa, tipoDoc));
        
        // 🔥 INYECTAMOS EL RASTRO DEL PERIODO Y CICLO
        if (request.cicloId() != null) {
            nuevoDocumento.setCicloFacturacion(cicloRepository.findById(request.cicloId()).orElse(null));
        }
        if (request.periodoId() != null) {
            nuevoDocumento.setPeriodoFacturacion(periodoRepository.findById(request.periodoId()).orElse(null));
        }

        try { nuevoDocumento.setMetadataJson(objectMapper.writeValueAsString(request)); } catch (Exception ignored) {}

        BigDecimal granTotal = BigDecimal.ZERO;
        BigDecimal granSaldo = BigDecimal.ZERO;

        for (LineaDetalle lineaDTO : lineasCalculadas) {
            Concepto concepto = conceptoRepository.findByCodigo(lineaDTO.conceptoCodigo()).orElseThrow();
            DocumentoDetalle detalle = new DocumentoDetalle();
            detalle.setConcepto(concepto);
            detalle.setCantidad(lineaDTO.cantidad());
            
            BigDecimal cant = lineaDTO.cantidad().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : lineaDTO.cantidad();
            detalle.setValorUnitario(lineaDTO.valorTotal().divide(cant, 2, java.math.RoundingMode.HALF_UP)); 
            detalle.setValorTotal(lineaDTO.valorTotal());
            detalle.setValorReal(lineaDTO.saldo()); 
            detalle.setSaldo(lineaDTO.saldo());
            detalle.setNaturaleza(lineaDTO.naturaleza());

            nuevoDocumento.addDetalle(detalle);
            
            if ("RESTA".equals(lineaDTO.naturaleza())) {
                granTotal = granTotal.subtract(detalle.getValorReal());
                granSaldo = granSaldo.subtract(detalle.getSaldo());
            } else {
                granTotal = granTotal.add(detalle.getValorReal());
                granSaldo = granSaldo.add(detalle.getSaldo());
            }
        }

        nuevoDocumento.setTotalDocumento(granTotal);
        nuevoDocumento.setSaldoDocumento(granSaldo);

        return documentoRepository.save(nuevoDocumento);
    }
    
    // ========================================================================
    // 3. 🔥 FACTURACIÓN MASIVA (BATCH BILLING)
    // ========================================================================
    @Transactional
    public Map<String, Object> liquidarLotePorCiclo(DocumentoDTO.LiquidarLoteRequest request) {
        log.info("Iniciando liquidación por lote. Ciclo ID: {}, Periodo ID: {}", request.cicloId(), request.periodoId());
        
        PeriodoFacturacion periodo = periodoRepository.findById(request.periodoId())
            .orElseThrow(() -> new RuntimeException("Periodo no encontrado"));
            
        // Regla estricta: Solo si está en modo líquido
        if (periodo.getEstado() != EstadoPeriodo.ABIERTO && periodo.getEstado() != EstadoPeriodo.LIQUIDANDO) {
            throw new RuntimeException("El periodo debe estar ABIERTO o LIQUIDANDO para poder facturar masivamente.");
        }

        List<SuscripcionPrograma> suscripciones = suscripcionRepo.findByCicloFacturacionIdAndActivoTrue(request.cicloId());
        if (suscripciones.isEmpty()) {
            throw new RuntimeException("No se encontraron suscripciones activas para este ciclo de facturación.");
        }

        int facturasGeneradas = 0;
        BigDecimal totalLote = BigDecimal.ZERO;

        for (SuscripcionPrograma sub : suscripciones) {
            // Ignorar comercios que no tienen tercero legal configurado
            if (sub.getEmpresa().getTercero() == null) continue;

            Map<String, BigDecimal> valoresOperativos = new HashMap<>();
            valoresOperativos.put("CANTIDAD_TABLETS", new BigDecimal(sub.getDispositivosActivos() != null ? sub.getDispositivosActivos() : 0));

            // Simulamos la misma petición que mandaría el FrontEnd
            CrearDocumentoRequest reqIndividual = new CrearDocumentoRequest(
                    request.empresaId(), // 🔥 FIX: El emisor de la factura es NODO MASTER, no el cliente.
                    0L, 
                    sub.getEmpresa().getTercero().getId(), // El cliente que recibe el cobro
                    "FV", 
                    request.codigoLiquidacion(),
                    valoresOperativos,
                    request.cicloId(),
                    request.periodoId()
                );

            try {
                Documento doc = generarDocumentoLiquidacion(reqIndividual);
                facturasGeneradas++;
                totalLote = totalLote.add(doc.getTotalDocumento());
            } catch (Exception e) {
                log.error("Error liquidando empresa " + sub.getEmpresa().getNombreComercial(), e);
            }
        }
        
        return Map.of(
            "facturasGeneradas", facturasGeneradas,
            "totalFacturado", totalLote,
            "mensaje", "Lote procesado exitosamente."
        );
    }

    // ... (El resto de métodos: reliquidarDocumento, generarConsecutivoSeguro, buscarPadrePorConsecutivo, emitirNota se quedan igual)
    // LOS DEJO AQUÍ PARA QUE COPIES Y PEGUES COMPLETO
    
    @Transactional
    public Documento reliquidarDocumento(Long idDocumentoOriginal) {
        Documento original = documentoRepository.findById(idDocumentoOriginal).orElseThrow();
        if ("ANULADO".equals(original.getEstado())) throw new RuntimeException("El documento ya se encuentra anulado.");

        CrearDocumentoRequest peticionOriginal;
        try {
            peticionOriginal = objectMapper.readValue(original.getMetadataJson(), CrearDocumentoRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer la metadata original.");
        }

        original.setEstado("ANULADO");
        original.setObservaciones("ANULADO POR RELIQUIDACIÓN.");
        original.setSaldoDocumento(BigDecimal.ZERO);
        original.getDetalles().forEach(det -> det.setSaldo(BigDecimal.ZERO));
        documentoRepository.save(original);

        Documento nuevoDocumento = generarDocumentoLiquidacion(peticionOriginal);
        nuevoDocumento.setDocumentoPadre(original);
        nuevoDocumento.setObservaciones("RELIQUIDACIÓN DE: " + original.getConsecutivo());
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
    
    @Transactional(readOnly = true)
    public Documento buscarPadrePorConsecutivo(Long empresaId, String consecutivo) {
        return documentoRepository.findByEmpresaIdAndConsecutivo(empresaId, consecutivo).orElseThrow();
    }

    @Transactional
    public Documento emitirNota(DocumentoDTO.EmitirNotaRequest request) {
        Empresa empresa = empresaRepository.findById(request.empresaId()).orElseThrow();
        Documento padre = documentoRepository.findById(request.documentoPadreId()).orElseThrow();
        TipoDocumento tipoDoc = tipoDocumentoRepository.findByCodigo(request.tipoNota()).orElseThrow();

        Documento nota = new Documento();
        nota.setEmpresa(empresa);
        nota.setPrograma(padre.getPrograma());
        nota.setTercero(padre.getTercero());
        nota.setTipoDocumento(tipoDoc);
        nota.setDocumentoPadre(padre);
        nota.setFechaEmision(LocalDateTime.now());
        nota.setEstado("APLICADO"); 
        nota.setObservaciones(request.observaciones());
        nota.setConsecutivo(generarConsecutivoSeguro(empresa, tipoDoc));

        BigDecimal granTotalNota = BigDecimal.ZERO;

        for (DocumentoDTO.DetalleNotaRequest detRequest : request.detalles()) {
            DocumentoDetalle detallePadre = padre.getDetalles().stream().filter(d -> d.getId().equals(detRequest.documentoDetallePadreId())).findFirst().orElseThrow();
            BigDecimal valorAjuste = detRequest.valorAjuste();

            if ("NC".equals(request.tipoNota())) {
                detallePadre.setSaldo(detallePadre.getSaldo().subtract(valorAjuste));
                padre.setSaldoDocumento(padre.getSaldoDocumento().subtract(valorAjuste));
            } else if ("ND".equals(request.tipoNota())) {
                detallePadre.setSaldo(detallePadre.getSaldo().add(valorAjuste));
                padre.setSaldoDocumento(padre.getSaldoDocumento().add(valorAjuste));
            }

            DocumentoDetalle detalleNota = new DocumentoDetalle();
            detalleNota.setConcepto(detallePadre.getConcepto());
            detalleNota.setCantidad(BigDecimal.ONE);
            detalleNota.setValorUnitario(valorAjuste);
            detalleNota.setValorTotal(valorAjuste);
            detalleNota.setValorReal(valorAjuste);
            detalleNota.setSaldo(valorAjuste); 
            detalleNota.setNaturaleza("NC".equals(request.tipoNota()) ? "RESTA" : "SUMA");

            nota.addDetalle(detalleNota);
            granTotalNota = granTotalNota.add(valorAjuste);
        }

        nota.setTotalDocumento(granTotalNota);
        nota.setSaldoDocumento(granTotalNota); 

        documentoRepository.save(padre); 
        return documentoRepository.save(nota);
    }
}