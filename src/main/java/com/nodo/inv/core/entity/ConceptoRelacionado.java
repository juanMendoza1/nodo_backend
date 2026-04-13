package com.nodo.inv.core.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "core_conceptos_relacionados")
public class ConceptoRelacionado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ccr_ideregistro")
    private Long id;

    // El concepto que TIENE la fórmula (Ej: IVA_19)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conc_padre_id", nullable = false)
    private Concepto conceptoPadre;

    // El concepto que es USADO DENTRO de la fórmula (Ej: CERV)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conc_hijo_id", nullable = false)
    private Concepto conceptoHijo;

    @Column(name = "ccr_tipo_relacion", length = 50)
    private String tipoRelacion; // Ej: "VARIABLE_DE_FORMULA", "IMPUESTO_APLICADO"
}