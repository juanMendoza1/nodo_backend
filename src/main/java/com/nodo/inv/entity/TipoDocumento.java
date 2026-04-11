package com.nodo.inv.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "con_tipo_documento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tdoc_ideregistro")
    private Long id;

    @Column(name = "tdoc_codigo", nullable = false, unique = true, length = 20)
    private String codigo; // Ej: "FV", "NC", "RC"

    @Column(name = "tdoc_nombre", nullable = false, length = 100)
    private String nombre; // Ej: "Factura de Venta", "Nota Crédito"

    @Column(name = "tdoc_naturaleza", length = 10)
    private String naturaleza; // "SUMA" o "RESTA"

    @Column(name = "tdoc_estado")
    private Boolean activo = true;
    
    @JsonIgnore
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "con_flujo_documento",
        joinColumns = @JoinColumn(name = "tdoc_origen_id"),
        inverseJoinColumns = @JoinColumn(name = "tdoc_permitido_id")
    )
    private Set<TipoDocumento> documentosPermitidos = new HashSet<>();
}