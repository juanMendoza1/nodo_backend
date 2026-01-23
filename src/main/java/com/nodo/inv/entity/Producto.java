package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "inv_producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa; // Aislamos los productos por billar/empresa

    @Column(name = "pro_codigo", nullable = false, length = 50)
    private String codigo; // SKU o Código de barras

    @Column(name = "pro_nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "pro_descripcion", length = 500)
    private String descripcion;

    // --- RELACIONES DINÁMICAS (Usando tu motor de parámetros) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_categoria")
    private Unidad categoria; // Ej: "Bebidas", "Snacks", "Alquiler Mesas"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_marca")
    private Unidad marca; // Ej: "Heineken", "Postobon"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_medida")
    private Unidad unidadMedida; // Ej: "Unidad", "Sixpack", "Hora"

    // --- DATOS FINANCIEROS ---

    @Column(name = "pro_preciocosto", precision = 18, scale = 2)
    private BigDecimal precioCosto;

    @Column(name = "pro_precioventa", precision = 18, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "pro_stockminimo")
    private Integer stockMinimo;

    @Column(name = "pro_estado")
    private Boolean activo; // Para descontinuar productos
}
