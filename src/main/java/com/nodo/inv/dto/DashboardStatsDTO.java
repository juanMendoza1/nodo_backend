package com.nodo.inv.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalProductos;
    private long productosBajoStock;
    private long terminalesActivas;
    private long personalActivo;
}