package com.nodo.inv.core.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "con_consecutivo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"emp_ideregistro", "tdoc_ideregistro"})
})
public class ConsecutivoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cons_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tdoc_ideregistro", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "cons_prefijo", length = 10, nullable = false)
    private String prefijo; // Ej: "FV", "NC", "LIQ"

    @Column(name = "cons_actual", nullable = false)
    private Long actual = 0L;
}