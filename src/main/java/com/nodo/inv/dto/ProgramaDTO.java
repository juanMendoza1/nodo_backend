package com.nodo.inv.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProgramaDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String version;
    private Boolean activo;
    private List<Long> permisosIds; 
    private List<String> permisosCodigos;
}