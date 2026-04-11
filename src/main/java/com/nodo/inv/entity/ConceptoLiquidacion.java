package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "liq_concepto_liquidacion")
public class ConceptoLiquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliq_ideregistro")
    private Long id;

    // La Plantilla de Liquidación que estamos usando
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liq_ideregistro", nullable = false)
    private Liquidacion liquidacion;

    // El Concepto que estamos agregando a la fórmula
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conc_ideregistro", nullable = false)
    private Concepto concepto;

    // MULTI-TENANT: ¿Qué empresa armó esta configuración?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    // RENDIMIENTO: ¿A qué programa pertenece este cruce? (Para queries más ágiles)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro")
    private Programa programa;

    // El orden en que el motor debe evaluar los conceptos (Ej: 1. Cervezas, 2. Descuentos, 3. IVA)
    @Column(name = "cliq_orden_calculo")
    private Integer ordenCalculo; 
}