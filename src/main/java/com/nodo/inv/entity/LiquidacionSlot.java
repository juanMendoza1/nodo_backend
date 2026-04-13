package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.entity.UsuarioOperativo;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "liq_liquidacion_slot")
public class LiquidacionSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liq_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro", nullable = false)
    private UsuarioOperativo usuarioSlot;

    // Rango de fechas que se está liquidando
    @Column(name = "liq_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "liq_fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "liq_fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    // --- DESGLOSE MATEMÁTICO ---
    @Column(name = "liq_base_ventas", precision = 12, scale = 2)
    private BigDecimal baseVentasCalculada; // Cuánto vendió en total en ese rango

    @Column(name = "liq_total_comision", precision = 12, scale = 2)
    private BigDecimal totalComision; // Lo que se ganó por porcentaje

    @Column(name = "liq_total_fijo", precision = 12, scale = 2)
    private BigDecimal totalFijo; // Lo que se ganó por días trabajados

    @Column(name = "liq_total_bonos", precision = 12, scale = 2)
    private BigDecimal totalBonos; // Suma de NovedadSlot tipo BONO

    @Column(name = "liq_total_descuentos", precision = 12, scale = 2)
    private BigDecimal totalDescuentos; // Suma de NovedadSlot tipo DESCUENTO/ANTICIPO

    @Column(name = "liq_gran_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal granTotalPagar; // La plata que se le entrega en la mano

    // "BORRADOR", "PAGADA", "ANULADA"
    @Column(name = "liq_estado", nullable = false, length = 20)
    private String estado; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_ideregistro")
    private Usuario generadaPor; // Qué Admin (Usuario Web) hizo este pago
}