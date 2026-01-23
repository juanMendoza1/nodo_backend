package com.nodo.inv.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "empresa_programa", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"emp_ideregistro", "pro_ideregistro"})
})
public class EmpresaPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ep_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Programa programa;

    @Column(name = "ep_fechaultimaactivacion")
    private LocalDateTime fechaActivacion;

    @Column(name = "ep_estado")
    private Boolean estado; // Para suspender un servicio específico a una empresa
}
