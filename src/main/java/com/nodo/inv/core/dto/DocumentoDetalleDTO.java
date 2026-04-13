package com.nodo.inv.core.dto;

import java.math.BigDecimal;

public record DocumentoDetalleDTO(
    String conceptoNombre,
    BigDecimal cantidad,
    BigDecimal valorUnitario,
    BigDecimal valorTotal, // Cantidad * Unitario
    BigDecimal valorReal,  // Valor total si es recaudable
    BigDecimal saldo,      // Lo que falta por pagar de esta línea
    String naturaleza      // SUMA / RESTA
) {}