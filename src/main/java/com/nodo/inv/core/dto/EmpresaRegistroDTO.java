package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class EmpresaRegistroDTO {
    // Datos del Tercero
    private String documento;
    private String nombre;
    private String apellido;
    private String correo;
    
    // Datos de la Empresa
    private String nombreComercial;
}