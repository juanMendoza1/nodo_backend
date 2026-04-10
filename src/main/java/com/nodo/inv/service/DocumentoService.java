package com.nodo.inv.service;

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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoService {

    // El Motor Matemático
    private final LiquidacionEngine liquidacionEngine;

    // Repositorios
    private final DocumentoRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ConceptoLiquidacionRepository recetaRepository;
    private final ConceptoRepository conceptoRepository;
    
    // Asumo que tienes estos repositorios básicos, si no, luego los creamos
    private final EmpresaRepository empresaRepository;
    private final ProgramaRepository programaRepository;
    private final TerceroRepository terceroRepository;

    /**
     * MÉTODO ORQUESTADOR: Construye una factura/documento desde cero usando el Motor.
     */
    @Transactional
    public Documento generarDocumentoLiquidacion(CrearDocumentoRequest request) {
        log.info("Iniciando liquidación para Empresa ID: {}, Programa ID: {}", request.empresaId(), request.programaId());

        // 1. OBTENER LAS PIEZAS BÁSICAS DE LA BD
        Empresa empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        Programa programa = programaRepository.findById(request.programaId())
                .orElseThrow(() -> new IllegalArgumentException("Programa no encontrado"));
        Tercero tercero = terceroRepository.findById(request.terceroId())
                .orElseThrow(() -> new IllegalArgumentException("Tercero no encontrado"));
        TipoDocumento tipoDoc = tipoDocumentoRepository.findByCodigo(request.tipoDocumentoCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento inválido"));

        // 2. BUSCAR LA "RECETA" (Las fórmulas y conceptos que configuró el Admin)
        List<ConceptoLiquidacion> receta = recetaRepository.obtenerRecetaDeLiquidacion(
                request.codigoLiquidacion(), 
                empresa.getId(), 
                programa.getId()
        );

        if (receta.isEmpty()) {
            throw new RuntimeException("No hay una plantilla de liquidación configurada para el código: " + request.codigoLiquidacion());
        }

        // 3. LLAMAR AL CEREBRO MATEMÁTICO (Aquí ocurre la magia)
        // Le pasamos la receta y los valores que mandó el Frontend (Ej: CERV -> 50000)
        List<LineaDetalle> lineasCalculadas = liquidacionEngine.ejecutarLiquidacion(receta, request.valoresOperativos());

        // 4. CREAR LA CABECERA DEL DOCUMENTO OFICIAL
        Documento nuevoDocumento = new Documento();
        nuevoDocumento.setEmpresa(empresa);
        nuevoDocumento.setPrograma(programa);
        nuevoDocumento.setTercero(tercero);
        nuevoDocumento.setTipoDocumento(tipoDoc);
        nuevoDocumento.setFechaEmision(LocalDateTime.now());
        nuevoDocumento.setEstado("EMITIDO");
        // Lógica de consecutivo (Se puede hacer más compleja después)
        nuevoDocumento.setConsecutivo(generarConsecutivo(tipoDoc.getCodigo(), empresa.getId()));

        // 5. TRADUCIR LA RESPUESTA DEL MOTOR A ENTIDADES DE BD (DocumentoDetalle)
        BigDecimal granTotal = BigDecimal.ZERO;
        BigDecimal granSaldo = BigDecimal.ZERO;

        for (LineaDetalle lineaDTO : lineasCalculadas) {
            // Buscamos el concepto real para enlazarlo
        	Concepto concepto = conceptoRepository.findByCodigo(lineaDTO.conceptoCodigo())
                    .orElseThrow(() -> new RuntimeException("Concepto no encontrado: " + lineaDTO.conceptoCodigo()));

            DocumentoDetalle detalle = new DocumentoDetalle();
            detalle.setConcepto(concepto);
            detalle.setCantidad(lineaDTO.cantidad());
            detalle.setValorUnitario(lineaDTO.valorTotal().divide(lineaDTO.cantidad())); // total / cant
            detalle.setValorTotal(lineaDTO.valorTotal());
            detalle.setValorReal(lineaDTO.saldo()); // Recuerda que el DTO mapeaba el valor real en saldo
            detalle.setSaldo(lineaDTO.saldo());
            detalle.setNaturaleza(tipoDoc.getNaturaleza());

            // Agregamos el detalle a la cabecera (Esto usa el método addDetalle que creamos en la entidad)
            nuevoDocumento.addDetalle(detalle);

            // Sumamos a los totales globales
            granTotal = granTotal.add(detalle.getValorTotal());
            granSaldo = granSaldo.add(detalle.getSaldo());
        }

        nuevoDocumento.setTotalDocumento(granTotal);
        nuevoDocumento.setSaldoDocumento(granSaldo);

        // 6. PERSISTIR EN LA BASE DE DATOS (El guardado final)
        log.info("Guardando documento total: ${}", granTotal);
        return documentoRepository.save(nuevoDocumento);
    }

    /**
     * Lógica temporal para generar consecutivos (Ej: FV-0001)
     * En un entorno real de alta concurrencia, esto usaría una tabla de secuencias.
     */
    private String generarConsecutivo(String prefijoDoc, Long empresaId) {
        long conteoActual = documentoRepository.count(); // Temporal para la prueba
        return prefijoDoc + "-" + String.format("%06d", conteoActual + 1);
    }
}