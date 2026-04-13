package com.nodo.inv.core.dto;

import lombok.Data;
import java.util.Map;

@Data
public class EventoOperativoDTO {
    // ID único generado por la app móvil (UUID) para evitar registrarlo dos veces si hay reintentos
    private String eventoId; 
    
    // Qué pasó: "PEDIDO_NUEVO", "DESPACHO", "CIERRE_CUENTA", "DUELO_INICIO"
    private String tipoEvento; 
    
    // La hora exacta en la que ocurrió en la tablet (System.currentTimeMillis())
    private Long fechaDispositivo; 
    
    // Aquí viene la "Carga Útil". Como es un Map, puede recibir cualquier JSON.
    // Si es un pedido, traerá IDs de productos. Si es un duelo, IDs de jugadores.
    private Map<String, Object> data; 
}