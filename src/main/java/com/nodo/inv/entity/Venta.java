package com.nodo.inv.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inv_venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro")
    private UsuarioOperativo usuarioOperativo; // Quién vendió

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "td_ideregistro")
    private TerminalDispositivo terminal; // Desde qué tablet

    private BigDecimal total;
    private String metodoPago; // EFECTIVO, QR, etc.
    private LocalDateTime fecha;
}