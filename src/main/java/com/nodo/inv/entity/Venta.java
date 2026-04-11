package com.nodo.inv.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "inv_venta")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ven_ideregistro")
    @EqualsAndHashCode.Include
    private Long id;

    // --- IDENTIFICACIÓN ÚNICA ---
    @Column(name = "ven_consecutivo", nullable = false, unique = true, length = 50)
    private String consecutivo; // Ej: VTA-0001024

    // --- SCOPE SAAS (MULTI-TENANT Y MÓDULOS) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Programa programa; // VITAL: ¿Esta venta vino del módulo RESTAURANTE, BILLAR o RETAIL?

    // --- ACTORES DE LA VENTA ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ter_cliente_id")
    private Tercero cliente; // Opcional: Si es venta rápida de mostrador queda null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro")
    private UsuarioOperativo usuarioOperativo; // Quién registró la venta (El Cajero/Mesero)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "td_ideregistro")
    private TerminalDispositivo terminal; // Desde qué tablet o PC se hizo

    // --- DATOS FINANCIEROS (FOTOGRAFÍA INMUTABLE) ---
    @Column(name = "ven_subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "ven_total_impuestos", precision = 15, scale = 2)
    private BigDecimal totalImpuestos;

    @Column(name = "ven_total_descuentos", precision = 15, scale = 2)
    private BigDecimal totalDescuentos;

    @Column(name = "ven_gran_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal granTotal;

    // --- OPERACIÓN Y AUDITORÍA ---
    @Column(name = "ven_metodo_pago", length = 50)
    private String metodoPago; // EFECTIVO, QR, TRANSFERENCIA, MULTIPLE

    @Column(name = "ven_estado", nullable = false, length = 20)
    private String estado; // PAGADA, PENDIENTE, ANULADA

    @Column(name = "ven_fecha_emision", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ven_referencia_origen", length = 100)
    private String referenciaOrigen; // Rastro: Ej: "PEDIDO_ID:45" o "DUELO_UUID:xyz"

    // 🔥 EL ESCAPE HATCH (FLEXIBILIDAD ABSOLUTA)
    @Column(name = "ven_metadata_json", columnDefinition = "TEXT")
    private String metadataJson; // Ej: {"mesa_local": 4, "turno": "T-01", "domiciliario": "Pedro"}

    // --- RELACIÓN CON EL DETALLE ---
    @ToString.Exclude
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaDetalle> detalles = new ArrayList<>();

    public void addDetalle(VentaDetalle detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}