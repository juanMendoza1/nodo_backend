package com.nodo.inv.dto;

import lombok.Data;

//DTO para la petición de generación
@Data
class GenerarQrRequest {
 private Long empresaId;
 private String programaCod;
}