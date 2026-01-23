package com.nodo.inv.dto;

import java.util.List;

import lombok.Data;

@Data
public class UsuarioRegistroDTO {
    private String login;
    private String password;
    private Long terceroId;
    private Long empresaId;
    private List<Long> rolesIds; // Para asignarle el rol ADMIN
}
