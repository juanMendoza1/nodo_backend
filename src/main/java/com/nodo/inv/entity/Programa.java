package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "programa")
public class Programa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_ideregistro")
    private Long id;

    @Column(name = "pro_codigo", nullable = false, unique = true, length = 50)
    private String codigo; // Ej: "INV", "POS_CORE"

    @Column(name = "pro_nombre", nullable = false, unique = true, length = 100)
    private String nombre; // Ej: "INVENTARIO BÁSICO", "PUNTO DE VENTA"

    @Column(name = "pro_descripcion", length = 500)
    private String descripcion; // Detalles de las funcionalidades del paquete

    @Column(name = "pro_version", length = 20)
    private String version; // Ej: "1.0.0"

    @Column(name = "pro_activo")
    private Boolean activo; // Para saber si el módulo se sigue comercializando
}