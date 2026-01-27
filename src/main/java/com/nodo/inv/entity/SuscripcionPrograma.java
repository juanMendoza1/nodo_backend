package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "inv_suscripcion_programa")
public class SuscripcionPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Programa programa;

    @Column(name = "sp_max_dispositivos", nullable = false)
    private Integer maxDispositivos; // Ejemplo: 5 (Cupos comprados)

    @Column(name = "sp_dispositivos_activos")
    private Integer dispositivosActivos; // Contador actual

    @Column(name = "sp_fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "sp_estado")
    private Boolean activo; // Para bloquear todo el servicio si no pagan
}