package com.nodo.inv.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.UsuarioOperativo;

@Getter
@Setter
@Entity
@Table(name = "pos_duelo")
public class Duelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "due_ideregistro")
    private Long id;

    @Column(name = "due_uuid", nullable = false, unique = true)
    private String uuidDuelo; // El UUID generado en la Tablet

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mes_ideregistro", nullable = false)
    private Mesa mesa; // ¿En qué mesa se jugó este duelo?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro")
    private UsuarioOperativo usuarioOperativo; // ¿Qué mesero lo abrió?

    @Column(name = "due_tipo_juego")
    private String tipoJuego; // "POOL" o "3BANDAS"

    @Column(name = "due_tarifa_tiempo", precision = 18, scale = 2)
    private BigDecimal tarifaTiempo;

    @Column(name = "due_regla_duelo")
    private String reglaDuelo;

    @Column(name = "due_estado")
    private String estado; // "EN_CURSO" o "FINALIZADO"

    @Column(name = "due_fecha_inicio")
    private Long fechaInicio;

    @Column(name = "due_fecha_fin")
    private Long fechaFin;
}