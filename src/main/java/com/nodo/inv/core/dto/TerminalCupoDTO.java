package com.nodo.inv.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TerminalCupoDTO {
    private Integer maxDispositivos;
    private Integer dispositivosActivos;
    private Integer disponibles;
}

