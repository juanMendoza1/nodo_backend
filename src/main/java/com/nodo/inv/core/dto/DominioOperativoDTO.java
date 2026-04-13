package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class DominioOperativoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String prefijoTablas;
    private String serviceProcessorBean;
    private Boolean activo;
}