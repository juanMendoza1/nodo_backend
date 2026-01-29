package com.nodo.inv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioSlotDTO {
    private Long id;
    private String nombreCompleto;
    private String login;
    private String passwordHash;
    private String rolUsuario;
}
