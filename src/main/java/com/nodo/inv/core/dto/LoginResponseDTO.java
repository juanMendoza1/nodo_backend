package com.nodo.inv.core.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {

	private String token;
    private String username;
    private Long empresaId;
    private String nombreEmpresa;
    private List<String> roles;
    private List<String> permisos;
    private List<String> programas; 
    private Long usuarioId;
    private Long terceroId;
}
