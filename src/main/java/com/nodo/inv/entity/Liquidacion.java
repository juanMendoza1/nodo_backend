package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "liq_liquidacion")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liq_ideregistro")
    private Long id;

    @Column(name = "liq_codigo", nullable = false, unique = true, length = 50)
    private String codigo; // Ej: "LIQ_VTA_BASIC"

    @Column(name = "liq_nombre", nullable = false, length = 100)
    private String nombre;

    // A qué programa (SaaS) pertenece esta plantilla de liquidación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Programa programa;

    @Column(name = "liq_es_global")
    private Boolean esGlobal = true; // Creada por el SuperAdmin
}