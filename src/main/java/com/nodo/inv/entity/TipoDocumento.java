package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;

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
}