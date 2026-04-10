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
import java.util.List;

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
    
    // 🔥 NUEVAS INYECCIONES PARA LOS RETOS
    private final ConsecutivoDocumentoRepository consecutivoRepository;
    private final ObjectMapper objectMapper; // Para convertir la petición a JSON

    /**
     * 1. MÉTODO ORQUESTADOR: Construye una factura nueva.
     */
    @Transactional
    public Documento generarDocumentoLiquidacion(CrearDocumentoRequest request) {
        log.info("Iniciando liquidación para Empresa ID: {}", request.empresaId());

        Empresa empresa = empresaRepository.findById(request.empresaId()).orElseThrow();
        Programa programa = programaRepository.findById(request.programaId()).orElseThrow();
        Tercero tercero = terceroRepository.findById(request.terceroId()).orElseThrow();
        TipoDocumento tipoDoc = tipoDocumentoRepository.findByCodigo(request.tipoDocumentoCodigo()).orElseThrow();

        List<ConceptoLiquidacion> receta = recetaRepository.obtenerRecetaDeLiquidacion(
                request.codigoLiquidacion(), empresa.getId(), programa.getId());

        if (receta.isEmpty()) {
            throw new RuntimeException("No hay una plantilla configurada para: " + request.codigoLiquidacion());
        }

        // Llamar al motor matemático
        List<LineaDetalle> lineasCalculadas = liquidacionEngine.ejecutarLiquidacion(receta, request.valoresOperativos());

        // Crear la cabecera
        Documento nuevoDocumento = new Documento();
        nuevoDocumento.setEmpresa(empresa);
        nuevoDocumento.setPrograma(programa);
        nuevoDocumento.setTercero(tercero);
        nuevoDocumento.setTipoDocumento(tipoDoc);
        nuevoDocumento.setFechaEmision(LocalDateTime.now());
        nuevoDocumento.setEstado("EMITIDO");
        
        // 🔥 SOLUCIÓN RETO 1: Generador Seguro de Consecutivos
        nuevoDocumento.setConsecutivo(generarConsecutivoSeguro(empresa, tipoDoc));

        // 🔥 SOLUCIÓN RETO 2: Guardamos el "ADN" de la petición original en el metadataJson
        try {
            String jsonOriginal = objectMapper.writeValueAsString(request);
            nuevoDocumento.setMetadataJson(jsonOriginal);
        } catch (Exception e) {
            log.warn("No se pudo serializar la petición original", e);
        }

        // Traducir las líneas
        BigDecimal granTotal = BigDecimal.ZERO;
        BigDecimal granSaldo = BigDecimal.ZERO;

        for (LineaDetalle lineaDTO : lineasCalculadas) {
            Concepto concepto = conceptoRepository.findByCodigo(lineaDTO.conceptoCodigo())
                    .orElseThrow(() -> new RuntimeException("Concepto no encontrado: " + lineaDTO.conceptoCodigo()));

            DocumentoDetalle detalle = new DocumentoDetalle();
            detalle.setConcepto(concepto);
            detalle.setCantidad(lineaDTO.cantidad());
            detalle.setValorUnitario(lineaDTO.valorTotal().divide(lineaDTO.cantidad())); 
            detalle.setValorTotal(lineaDTO.valorTotal());
            detalle.setValorReal(lineaDTO.saldo()); 
            detalle.setSaldo(lineaDTO.saldo());
            detalle.setNaturaleza(tipoDoc.getNaturaleza());

            nuevoDocumento.addDetalle(detalle);
            granTotal = granTotal.add(detalle.getValorTotal());
            granSaldo = granSaldo.add(detalle.getSaldo());
        }

        nuevoDocumento.setTotalDocumento(granTotal);
        nuevoDocumento.setSaldoDocumento(granSaldo);

        return documentoRepository.save(nuevoDocumento);
    }

    /**
     * 2. EL NUEVO PODER: Reliquidar un documento existente
     */
    @Transactional
    public Documento reliquidarDocumento(Long idDocumentoOriginal) {
        // 1. Buscamos el documento original
        Documento original = documentoRepository.findById(idDocumentoOriginal)
                .orElseThrow(() -> new RuntimeException("Documento original no encontrado"));

        if ("ANULADO".equals(original.getEstado())) {
            throw new RuntimeException("El documento ya se encuentra anulado. No se puede reliquidar de nuevo.");
        }

        // 2. Extraemos el "ADN" (La petición original)
        CrearDocumentoRequest peticionOriginal;
        try {
            peticionOriginal = objectMapper.readValue(original.getMetadataJson(), CrearDocumentoRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer la metadata original para realizar la reliquidación.");
        }

        // 3. Anulamos el viejo documento (Nunca borramos en contabilidad, solo anulamos)
        original.setEstado("ANULADO");
        original.setObservaciones("ANULADO POR RELIQUIDACIÓN. Saldos en cero.");
        original.setSaldoDocumento(BigDecimal.ZERO); // Borramos el saldo para que no aparezca en cartera
        
        // Ponemos el saldo de cada línea en cero también
        original.getDetalles().forEach(det -> det.setSaldo(BigDecimal.ZERO));
        
        documentoRepository.save(original);

        // 4. Generamos el nuevo documento clonando la petición (Pasará por el motor de nuevo y aplicará las fórmulas actualizadas)
        Documento nuevoDocumento = generarDocumentoLiquidacion(peticionOriginal);
        
        // 5. Los vinculamos para mantener el rastro de auditoría
        nuevoDocumento.setDocumentoPadre(original);
        nuevoDocumento.setObservaciones("RELIQUIDACIÓN DEL DOCUMENTO: " + original.getConsecutivo());
        
        return documentoRepository.save(nuevoDocumento);
    }

    /**
     * 3. LÓGICA DE CONSECUTIVO SEGURO (Bloqueo Pesimista)
     */
    private String generarConsecutivoSeguro(Empresa empresa, TipoDocumento tipoDoc) {
        // Busca el contador en la BD, o lo crea desde cero si es la primera factura de esa empresa
        ConsecutivoDocumento consecutivo = consecutivoRepository
                .findByEmpresaAndTipoDocumentoForUpdate(empresa.getId(), tipoDoc.getId())
                .orElseGet(() -> {
                    ConsecutivoDocumento nuevo = new ConsecutivoDocumento();
                    nuevo.setEmpresa(empresa);
                    nuevo.setTipoDocumento(tipoDoc);
                    nuevo.setPrefijo(tipoDoc.getCodigo()); // Ej: "FV"
                    nuevo.setActual(0L);
                    return consecutivoRepository.save(nuevo);
                });

        // Sumamos 1 al contador
        consecutivo.setActual(consecutivo.getActual() + 1);
        consecutivoRepository.save(consecutivo);

        // Retornamos formateado (Ej: FV-000001)
        return consecutivo.getPrefijo() + "-" + String.format("%06d", consecutivo.getActual());
    }
}