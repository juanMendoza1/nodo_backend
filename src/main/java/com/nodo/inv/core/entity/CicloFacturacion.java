package com.nodo.inv.core.entity;

import com.nodo.inv.Utils.FrecuenciaCiclo;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "fac_ciclo")
public class CicloFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ciclo_ideregistro")
    private Long id;

    @Column(name = "ciclo_nombre", nullable = false, unique = true, length = 100)
    private String nombre; // Ej: "Suscripción Mensual Billares"

    @Enumerated(EnumType.STRING)
    @Column(name = "ciclo_frecuencia", nullable = false, length = 20)
    private FrecuenciaCiclo frecuencia;

    @Column(name = "ciclo_dia_corte", nullable = false)
    private Integer diaCorte; // Ej: 30 (Para saber qué día del mes se corta)

    @Column(name = "ciclo_dias_gracia", nullable = false)
    private Integer diasGracia; // Ej: 5 (Para calcular la fecha límite de pago automáticamente)

    @Column(name = "ciclo_activo", nullable = false)
    private Boolean activo = true;
}