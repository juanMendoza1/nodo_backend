package com.nodo.inv.entity;

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

    @Column(name = "cla_descripcion")
    private String descripcion;
}