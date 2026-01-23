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
    private Empresa empresa; // Amarra el producto a "Billares Diego"

    @Column(name = "pro_codigo", nullable = false, length = 50)
    private String codigo; // SKU o Código de barras

    @Column(name = "pro_nombre", nullable = false, length = 200)
    private String nombre;

    // --- RELACIONES CON TU MOTOR DE PARÁMETROS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_categoria")
    private Unidad categoria; // Ej: "Cervezas" (Vinculado a Unidad/Estructura)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_medida")
    private Unidad unidadMedida; // Ej: "Botella 330ml", "Unidad"

    // --- DATOS FINANCIEROS ---
    @Column(name = "pro_preciocosto", precision = 18, scale = 2)
    private BigDecimal precioCosto;

    @Column(name = "pro_precioventa", precision = 18, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "pro_stockminimo")
    private Integer stockMinimo; // Alerta cuando Diego se esté quedando sin producto

    @Column(name = "pro_estado")
    private Boolean activo;
    
    @Column(name = "pro_stockactual")
    private Integer stockActual;
}