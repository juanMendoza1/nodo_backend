package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "liq_acuerdo_pago_slot")
public class AcuerdoPagoSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aps_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro", nullable = false)
    private UsuarioOperativo usuarioSlot;

    @Column(name = "aps_radicado", nullable = false, unique = true, length = 50)
    private String radicado; 

    @Column(name = "aps_tipo_acuerdo", nullable = false, length = 50)
    private String tipoAcuerdo; 

    @Column(name = "aps_valor_fijo_dia", precision = 12, scale = 2)
    private BigDecimal valorFijoDia;

    @Column(name = "aps_porcentaje_comision", precision = 5, scale = 2)
    private BigDecimal porcentajeComision;

    @Column(name = "aps_estado", nullable = false, length = 20)
    private String estado; 

    @Column(name = "aps_fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // 🔥 NUEVOS CAMPOS
    @Column(name = "aps_fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "aps_fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "aps_frecuencia_pago", length = 30)
    private String frecuenciaPago;

    @Column(name = "aps_observaciones", length = 500)
    private String observaciones;
}