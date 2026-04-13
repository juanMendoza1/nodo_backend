package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class GiroNegocioDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String templateMovil;
}