package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class UsuarioDataDTO {
    private Long id;
    private String login;
    private String password; // Opcional en el PUT, obligatorio en el POST
    private String estado;   // Recibe "ACTIVO", "INACTIVO" o "BLOQUEADO"
    
    // Objetos anidados que envía el SearchableSelect desde React
    private RelacionDTO tercero;
    private RelacionDTO empresa;
    private Long rolId;

    // Clase interna para leer { id: X } de los JSON
    @Data
    public static class RelacionDTO {
        private Long id;
    }
}