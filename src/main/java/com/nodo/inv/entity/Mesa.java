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
    private Empresa empresa;

    @Column(name = "mes_id_local")
    private Integer idMesaLocal; 

    @Column(name = "mes_nombre", length = 50)
    private String nombre; 

    @Column(name = "mes_estado")
    private String estado; // "DISPONIBLE", "ABIERTO", "OCUPADA"

    // 🔥 REGRESAMOS EL TIPO DE JUEGO PARA QUE LA MESA TENGA MEMORIA EN ESTADO "ABIERTO"
    @Column(name = "mes_tipo_juego", length = 50)
    private String tipoJuego; 

    @Column(name = "mes_tarifa_tiempo", precision = 18, scale = 2)
    private BigDecimal tarifaTiempo; 

    @Column(name = "mes_fecha_apertura")
    private Long fechaApertura; 

    @Column(name = "mes_fecha_cierre")
    private Long fechaCierre; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uop_ideregistro")
    private UsuarioOperativo usuarioActual; 
}