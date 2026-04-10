package com.nodo.inv.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// EL PARAGUAS PRINCIPAL: Lo que se devuelve cuando consultan una factura ya guardada
public record DocumentoDTO(
    Long id,
    String consecutivo,
    String tipoDocumentoCodigo,
    String tipoDocumentoNombre,
    String terceroNombre,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    BigDecimal total,
    BigDecimal saldo,
    String estado,
    List<DocumentoDetalleDTO> detalles,
    String metadataJson
) {

    // 1. EL DTO DE PETICIÓN (REQUEST): Lo que el Frontend envía para liquidar
    public record CrearDocumentoRequest(
        Long empresaId,
        Long programaId,
        Long terceroId,
        String tipoDocumentoCodigo, // Ej: "FV" (Factura Venta)
        String codigoLiquidacion,   // Ej: "LIQ_POS_01" (Para saber qué fórmula aplicar)
        
        // Aquí viene la plata cruda. Ej: {"CERV" -> 50000, "HORA_BILLAR" -> 20000}
        Map<String, BigDecimal> valoresOperativos 
    ) {}

    // 2. EL DTO INTERNO DEL MOTOR: Lo que el LiquidacionEngine calcula en memoria
    public record LineaDetalle(
    	String conceptoCodigo,	
        String conceptoNombre,
        BigDecimal cantidad,
        BigDecimal valorTotal,
        BigDecimal saldo
    ) {}
}