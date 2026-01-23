package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "empresa_tercero")
public class EmpresaTercero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "et_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro") // Puede ser null si es un tercero global del sistema
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ter_ideregistro", nullable = false)
    private Tercero tercero;

    @Column(name = "et_es_global", nullable = false)
    private Boolean esGlobal = false;

    @Column(name = "et_fecha_vinculo")
    private LocalDateTime fechaVinculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_ideregistro") // Quién lo creó (Diego o Juan)
    private Usuario creadoPor;
}