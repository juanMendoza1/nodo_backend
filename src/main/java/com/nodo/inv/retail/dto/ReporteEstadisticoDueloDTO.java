package com.nodo.inv.retail.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ReporteEstadisticoDueloDTO {
    private int idMesa;
    private String uuidDuelo;
    private String tipoJuego;
    private long timestampFin;
    private Map<String, Integer> resumenGeneral; // <NombreEquipo, PuntosTotales>
    private Map<String, DetalleEquipo> detalleEstadistico; // <NombreEquipo, Datos>

    @Data
    public static class DetalleEquipo {
        private int puntosTotales;
        private int puntosPositivos;
        private int cantidadMalas;
        private List<Integer> bolasAnotadas;
    }
}