package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "per_ideregistro")
    private Long id;

    @Column(name = "per_codigo", nullable = false, unique = true, length = 50)
    private String codigo; // INV_VIEW, INV_CREA, etc.

    @Column(name = "per_descripcion", length = 200)
    private String descripcion;

    // getters y setters
}