package com.nodo.inv.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
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

    @Column(name = "pro_nombre", nullable = false, unique = true, length = 100)
    private String nombre; // Ej: "INVENTARIO", "REGISTRO_TERCEROS"

    @Column(name = "pro_codigo", nullable = false, unique = true, length = 10)
    private String codigo; // Ej: "INV", "TER"

    @Column(name = "pro_activo")
    private Boolean activo;
}