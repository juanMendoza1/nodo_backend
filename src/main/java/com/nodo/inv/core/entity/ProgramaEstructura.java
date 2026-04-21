package com.nodo.inv.core.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "programa_estructura")
public class ProgramaEstructura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pe_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Programa programa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "est_ideregistro", nullable = false)
    private Estructura estructura;
}