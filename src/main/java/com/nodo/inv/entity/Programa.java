// src/main/java/com/nodo/inv/entity/Programa.java
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
    private String codigo; 

    @Column(name = "pro_nombre", nullable = false, unique = true, length = 100)
    private String nombre; 

    @Column(name = "pro_descripcion", length = 500)
    private String descripcion; 

    @Column(name = "pro_version", length = 20)
    private String version; 

    @Column(name = "pro_activo")
    private Boolean activo; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dom_ideregistro")
    private DominioOperativo dominio;
}