package com.nodo.inv.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column(name = "cla_nombre", nullable = false, length = 100)
    private String nombre; // Ej: "TERCEROS"

    @Column(name = "cla_descripcion")
    private String descripcion;
}