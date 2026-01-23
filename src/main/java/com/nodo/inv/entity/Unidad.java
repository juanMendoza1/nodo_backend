package com.nodo.inv.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "unidad")
public class Unidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uni_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "est_ideregistro", nullable = false)
    private Estructura estructura;

    
    @Column(name = "uni_codigo", nullable = false, length = 100)
    private String codigo; // Ej: "CEDULA DE CIUDADANIA", "NIT"
    
    
    @Column(name = "uni_nombre", nullable = false, length = 100)
    private String nombre; // Ej: "CEDULA DE CIUDADANIA", "NIT"
    
    
}
