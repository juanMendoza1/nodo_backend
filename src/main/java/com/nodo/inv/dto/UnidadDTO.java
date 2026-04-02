package com.nodo.inv.dto;

import lombok.Data;

@Data
public class UnidadDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String estructuraCodigo; // Ej: "CAT_PROD" o "UNI_MED"
}