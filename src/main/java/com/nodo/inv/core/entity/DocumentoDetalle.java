package com.nodo.inv.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "con_documento_detalle")
public class DocumentoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ddet_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_ideregistro", nullable = false)
    @JsonIgnore
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conc_ideregistro", nullable = false)
    private Concepto concepto;

    @Column(name = "ddet_cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad; 

    @Column(name = "ddet_valor_unitario", precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "ddet_valor_total", precision = 15, scale = 2)
    private BigDecimal valorTotal; // cantidad * valorUnitario

    @Column(name = "ddet_valor_real", precision = 15, scale = 2)
    private BigDecimal valorReal; // = valorTotal (si recaudo: S), sino 0

    @Column(name = "ddet_saldo", precision = 15, scale = 2)
    private BigDecimal saldo; // Nace igual al valorReal, baja con los pagos

    @Column(name = "ddet_naturaleza", length = 10)
    private String naturaleza; // "SUMA" o "RESTA"
}