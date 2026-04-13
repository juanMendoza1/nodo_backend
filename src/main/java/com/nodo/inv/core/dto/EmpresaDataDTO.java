package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class EmpresaDataDTO {
    private Long id;
    private String nombreComercial;
    private Boolean activo;
    private RelacionDTO tercero;
    private RelacionDTO giroNegocio;

    @Data
    public static class RelacionDTO {
        private Long id;
    }
}