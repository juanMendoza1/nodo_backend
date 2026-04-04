package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "pos_mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mes_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa; // Vinculación con la empresa

    @Column(name = "mes_id_local")
    private Integer idMesaLocal; // El ID que viene de la App móvil

    @Column(name = "mes_nombre", length = 50)
    private String nombre; // Ej: "Mesa 1", "Mesa VIP"

    @Column(name = "mes_estado")
    private String estado; // "ABIERTO", "CERRADO", "INACTIVO"

    @Column(name = "mes_tipo_juego")
    private String tipoJuego; // "POOL" o "3BANDAS"

    @Column(name = "mes_tarifa_tiempo", precision = 18, scale = 2)
    private BigDecimal tarifaTiempo; // Costo por hora

    @Column(name = "mes_regla_duelo")
    private String reglaDuelo; // "GANADOR_SALVA", etc.

    @Column(name = "mes_fecha_apertura")
    private Long fechaApertura; // Timestamp de apertura

    @Column(name = "mes_fecha_cierre")
    private Long fechaCierre; // Timestamp de cierre

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_ideregistro")
    private Usuario usuarioActual; // Usuario que tiene abierta la mesa
}