package com.nodo.inv.core.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SuscripcionDTO {
    private Long id;
    private Integer maxDispositivos;
    private Integer dispositivosActivos;
    private LocalDateTime fechaVencimiento;
    private Boolean activo;

    private EmpresaMinDTO empresa;
    private ProgramaMinDTO programa;
    private CicloMinDTO cicloFacturacion; // 🔥 Ahora el DTO ya conoce el ciclo
    private LiquidacionMinDTO liquidacion;

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

    // 🔥 ESTA ES LA CLASE QUE FALTABA Y CAUSABA EL ERROR
    @Data
    public static class CicloMinDTO {
        private Long id;
        private String nombre;
    }
    
    @Data
    public static class LiquidacionMinDTO {
        private Long id;
        private String codigo;
        private String nombre;
    }
    
}