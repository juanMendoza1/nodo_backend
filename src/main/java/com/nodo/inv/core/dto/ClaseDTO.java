package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class ClaseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo; // Aunque no esté en la entidad actual, siempre es buena práctica recibirlo
}