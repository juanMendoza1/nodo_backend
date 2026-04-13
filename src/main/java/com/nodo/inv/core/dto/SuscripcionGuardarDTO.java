package com.nodo.inv.core.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SuscripcionGuardarDTO {
    private Long id;
    private Integer maxDispositivos;
    private Boolean activo;
    private LocalDateTime fechaVencimiento;
    
    private RelacionDTO empresa;
    private RelacionDTO programa;

    @Data
    public static class RelacionDTO {
        private Long id;
    }
}