package com.nodo.inv.nomina.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.UsuarioOperativo;

@Data
@Entity
@Table(name = "liq_novedad_slot")
public class NovedadSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nov_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro", nullable = false)
    private UsuarioOperativo usuarioSlot;

    // 🔥 NUEVO: Ahora la nota es hija estricta del contrato
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aps_ideregistro")
    private AcuerdoPagoSlot acuerdoPago;

    @Column(name = "nov_tipo", nullable = false, length = 30)
    private String tipoNovedad;

    @Column(name = "nov_descripcion", nullable = false, length = 200)
    private String descripcion; 

    @Column(name = "nov_valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "nov_fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "nov_aplicada")
    private Boolean aplicada = false; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liq_ideregistro")
    private LiquidacionSlot liquidacionSlot; 
}