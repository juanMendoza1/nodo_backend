package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class EstructuraDTO {
    private Long id;
    private String codigo;
    private String nombre;
    // El frontend nos envía esto cuando seleccionamos en el SearchableSelect
    private ClaseRelacionDTO clase; 

    @Data
    public static class ClaseRelacionDTO {
        private Long id;
    }
}