package com.nodo.inv.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inv_venta_detalle")
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vdet_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ven_ideregistro", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro")
    private Producto producto; // Puede ser NULL si es un cobro dinámico (Ej: Alquiler de mesa)

    // --- FOTOGRAFÍA DEL ITEM ---
    @Column(name = "vdet_nombre_item", nullable = false, length = 200)
    private String nombreItem; // Si el producto se borra mañana, aquí queda guardado el nombre histórico

    // 🔥 LA CONEXIÓN UNIVERSAL PARAMÉTRICA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_tipo_item", nullable = false)
    private Unidad tipoItem; // Ej: ID que apunta a "PRODUCTO", "SERVICIO", "TIEMPO", "PROPINA"

    // --- MATEMÁTICA DE LA LÍNEA ---
    @Column(name = "vdet_cantidad", precision = 12, scale = 2, nullable = false)
    private BigDecimal cantidad; // BigDecimal para poder vender "1.5" horas o "2.5" kilos

    @Column(name = "vdet_precio_unitario", precision = 15, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "vdet_subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal; // cantidad * precioUnitario

    @Column(name = "vdet_impuesto_aplicado", precision = 15, scale = 2)
    private BigDecimal impuestoAplicado;

    @Column(name = "vdet_descuento_aplicado", precision = 15, scale = 2)
    private BigDecimal descuentoAplicado;

    @Column(name = "vdet_total_linea", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalLinea; // subtotal + impuesto - descuento

    // 🔥 ESCAPE HATCH DEL DETALLE
    @Column(name = "vdet_metadata_json", columnDefinition = "TEXT")
    private String metadataJson; // Ej: {"jugador_perdedor": "Juan", "vendedor_comision": "Alejo"}
}