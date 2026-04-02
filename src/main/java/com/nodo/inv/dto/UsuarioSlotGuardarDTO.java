package com.nodo.inv.dto;

import lombok.Data;

@Data
public class UsuarioSlotGuardarDTO {
    private Long id;
    private String alias;   // Ej: "MESERO ALEJO"
    private String login;   // Ej: "M1_ALEJO"
    private String password; // PIN de 4 dígitos (Solo si se va a cambiar o es nuevo)
}