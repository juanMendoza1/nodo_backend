package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class CicloFacturacionDTO {
    private Long id;
    private String nombre;
    private String frecuencia; // MENSUAL, BIMENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL
    private Integer diaCorte;
    private Integer diasGracia;
    private Boolean activo;
}