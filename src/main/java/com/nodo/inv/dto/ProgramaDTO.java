package com.nodo.inv.dto;

import lombok.Data;

@Data
public class ProgramaDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String version;
    private Boolean activo;
}