package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "liq_novedad_slot")
public class NovedadSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nov_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa; // Aislamiento SaaS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro", nullable = false)
    private UsuarioOperativo usuarioSlot;

    // "DESCUENTO", "ANTICIPO", "BONO", "PROPINA_EXTRA"
    @Column(name = "nov_tipo", nullable = false, length = 30)
    private String tipoNovedad;

    @Column(name = "nov_descripcion", nullable = false, length = 200)
    private String descripcion; // Ej: "Vaso roto mesa 4"

    @Column(name = "nov_valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "nov_fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    // Cuando se le pague al mesero, esto pasa a TRUE para no volver a descontárselo
    @Column(name = "nov_aplicada")
    private Boolean aplicada = false; 

    // Relación opcional para saber en qué liquidación se cobró/pagó esta novedad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liq_ideregistro")
    private LiquidacionSlot liquidacionSlot; 
}