package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inv_movimiento")
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mov_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @Column(name = "mov_cantidad", nullable = false)
    private Integer cantidad; // Positivo para entradas, negativo para salidas

    @Column(name = "mov_tipo", nullable = false)
    private String tipo; // EJ: "VENTA_POS", "DESPACHO_MESA", "COMPRA", "AJUSTE"

    @Column(name = "mov_fecha")
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_ideregistro")
    private Usuario usuario; // Quién realizó el movimiento

    @Column(name = "mov_referencia_externa")
    private String referenciaExterna; // Aquí guardaremos el 'idDuelo' o 'idVenta' de la App Móvil
}