package com.nodo.inv.dto;

import lombok.Data;

@Data
public class UnidadDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String estructuraCodigo; 
    private Long empresaId; 
    private Boolean esGlobal;
}