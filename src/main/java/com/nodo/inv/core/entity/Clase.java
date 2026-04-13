package com.nodo.inv.core.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clase")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cla_ideregistro")
    private Long id;

    // Campo indispensable para que DataInitializer pueda buscar por código (ej: "INV")
    @Column(name = "cla_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "cla_nombre", nullable = false, length = 100)
    private String nombre; // Ej: "INVENTARIO"

    @Column(name = "cla_descripcion", length = 500)
    private String descripcion;

    // 🔥 NUEVO: El campo que le faltaba al DataInitializer y al Frontend
    @Column(name = "cla_activo")
    private Boolean activo;
}