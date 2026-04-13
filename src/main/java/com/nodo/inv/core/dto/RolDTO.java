// src/main/java/com/nodo/inv/dto/RolDTO.java
package com.nodo.inv.core.dto;

import lombok.Data;

@Data
public class RolDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
