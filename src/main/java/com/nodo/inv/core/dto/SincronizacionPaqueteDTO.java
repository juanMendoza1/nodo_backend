package com.nodo.inv.core.dto;

import lombok.Data;
import java.util.List;

@Data
public class SincronizacionPaqueteDTO {
    // El UUID del dispositivo (tablet) que está mandando la info
    private String terminalUuid; 
    
    // Para respetar tu arquitectura multi-empresa (SaaS)
    private Long empresaId;      
    
    // La lista de todas las cosas que pasaron mientras la tablet no se había sincronizado
    private List<EventoOperativoDTO> eventos;
}