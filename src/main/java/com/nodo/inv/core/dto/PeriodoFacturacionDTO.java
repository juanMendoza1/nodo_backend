package com.nodo.inv.core.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PeriodoFacturacionDTO {
    private Long id;
    private Long cicloId;
    private String cicloNombre;
    private Integer mesOrigen;
    private Integer anioOrigen;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaCorte;
    private LocalDate fechaVencimientoPago;
    private String estado; // EN_ESPERA, ABIERTO, LIQUIDANDO, CERRADO
}