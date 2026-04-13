package com.nodo.inv.retail.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.nodo.inv.retail.entity.Mesa;

@Data
@NoArgsConstructor
public class MesaDTO {
    private Long id;
    private Integer idMesaLocal;
    private String nombre;
    private String estado;
    private String tipoJuego;
    private BigDecimal tarifaTiempo;
    private Long fechaApertura;

    // Constructor que convierte la Entidad en DTO fácilmente
    public MesaDTO(Mesa mesa) {
        this.id = mesa.getId();
        this.idMesaLocal = mesa.getIdMesaLocal();
        this.nombre = mesa.getNombre();
        this.estado = mesa.getEstado();
        this.tipoJuego = mesa.getTipoJuego();
        this.tarifaTiempo = mesa.getTarifaTiempo();
        this.fechaApertura = mesa.getFechaApertura();
    }
}