package com.nodo.inv.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MovimientoDTO {
    private Long id;
    private LocalDateTime fecha;
    private String tipo; // Ej: DESPACHO_MESA, INGRESO_COMPRA
    private Integer cantidad;
    private String productoNombre;
    private String creador; // Quién hizo el movimiento
    private String referencia;
}