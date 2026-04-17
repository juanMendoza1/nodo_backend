package com.nodo.inv.core.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.nodo.inv.Utils.Naturaleza;

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

    // 1. PETICIÓN INDIVIDUAL
    public record CrearDocumentoRequest(
        Long empresaId,
        Long programaId,
        Long terceroId,
        String tipoDocumentoCodigo, 
        String codigoLiquidacion,   
        Map<String, BigDecimal> valoresOperativos,
        Long cicloId,   
        Long periodoId ,
        Long suscripcionId,
        String estadoDocumento
    ) {}

    public record LiquidarLoteRequest(
        	Long empresaId,	
            Long cicloId,
            Long periodoId
        ) {}

    public record LineaDetalle(
    	String conceptoCodigo,	
        String conceptoNombre,
        BigDecimal cantidad,
        BigDecimal valorTotal,
        BigDecimal saldo,
        Naturaleza naturaleza
    ) {}
    
    public record EmitirNotaRequest(
        Long empresaId,
        Long documentoPadreId,
        String tipoNota,
        String observaciones,
        List<DetalleNotaRequest> detalles
    ) {}

    public record DetalleNotaRequest(
        Long documentoDetallePadreId,
        BigDecimal valorAjuste
    ) {}
}