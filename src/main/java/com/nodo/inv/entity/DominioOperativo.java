package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "core_dominio_operativo")
public class DominioOperativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dom_ideregistro")
    private Long id;

    @Column(name = "dom_codigo", unique = true, nullable = false, length = 50)
    private String codigo; // Ej: RETAIL, UTILITIES, HOSPITALITY

    @Column(name = "dom_nombre", nullable = false, length = 100)
    private String nombre; // Ej: Ventas y Retail

    @Column(name = "dom_prefijo_tablas", length = 20)
    private String prefijoTablas; // Ej: inv_ , sp_ , htl_

    @Column(name = "dom_bean_procesador", length = 100)
    private String serviceProcessorBean; // Ej: retailSyncProcessor

    @Column(name = "dom_activo")
    private Boolean activo = true;
}