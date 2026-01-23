package com.nodo.inv.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "estructura")
public class Estructura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "est_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cla_ideregistro", nullable = false)
    private Clase clase;

    @Column(name = "est_nombre", nullable = false, length = 100)
    private String nombre; // Ej: "TIPOS DE IDENTIFICACION"
}