package com.nodo.inv.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SuscripcionDTO {
    private Long id;
    private Integer maxDispositivos;
    private Integer dispositivosActivos;
    private LocalDateTime fechaVencimiento;
    private Boolean activo;
    
    // Objetos anidados planos para el SearchableSelect del Frontend
    private EmpresaMinDTO empresa;
    private ProgramaMinDTO programa;

    @Data
    public static class EmpresaMinDTO {
        private Long id;
        private String nombreComercial;
    }

    @Data
    public static class ProgramaMinDTO {
        private Long id;
        private String codigo;
        private String nombre;
    }
}