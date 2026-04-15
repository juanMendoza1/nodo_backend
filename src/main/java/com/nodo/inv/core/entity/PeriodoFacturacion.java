package com.nodo.inv.core.entity;

import com.nodo.inv.Utils.EstadoPeriodo;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fac_periodo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ciclo_ideregistro", "per_mes_origen", "per_anio_origen"})
})
public class PeriodoFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "per_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_ideregistro", nullable = false)
    private CicloFacturacion ciclo;

    @Column(name = "per_mes_origen", nullable = false)
    private Integer mesOrigen; // 1 a 12

    @Column(name = "per_anio_origen", nullable = false)
    private Integer anioOrigen; // Ej: 2026

    @Column(name = "per_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "per_fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "per_fecha_corte", nullable = false)
    private LocalDateTime fechaCorte; // Día exacto y hora de la foto de cobro

    @Column(name = "per_fecha_vencimiento_pago", nullable = false)
    private LocalDate fechaVencimientoPago; // Día límite de pago

    @Enumerated(EnumType.STRING)
    @Column(name = "per_estado", nullable = false, length = 20)
    private EstadoPeriodo estado;
}