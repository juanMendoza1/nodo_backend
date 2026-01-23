package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inv_giro_negocio")
public class GiroNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gn_ideregistro")
    private Long id;

    @Column(name = "gn_nombre", nullable = false, length = 100)
    private String nombre; // Ej: "RESTAURANTE / BAR", "ZAPATERÍA", "RETAIL"

    @Column(name = "gn_codigo", nullable = false, unique = true, length = 20)
    private String codigo; // Ej: "REST", "ZAPA", "RET"

    @Column(name = "gn_descripcion")
    private String descripcion;

    // Este campo define qué interfaz cargará la Tablet automáticamente
    @Column(name = "gn_template_movil")
    private String templateMovil; // Ej: "ARENA_DUELO", "POS_ESTANDAR", "CATALOGO_ZAPATOS"
}