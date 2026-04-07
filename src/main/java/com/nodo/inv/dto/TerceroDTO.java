package com.nodo.inv.dto;

import lombok.Data;

@Data
public class TerceroDTO {
    private Long id;
    private String documento;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    
    // Estos son los IDs paramétricos que llegan desde los Selects del Frontend
    private Long tipoTerceroId;
    private Long tipoIdentificacionId;
}