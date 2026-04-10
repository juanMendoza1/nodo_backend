package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "con_documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro", nullable = false)
    private Programa programa; // Origen del documento (Ej: NÓMINA_SLOT, POS_BILLAR)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ter_ideregistro", nullable = false)
    private Tercero tercero; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tdoc_ideregistro", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "doc_consecutivo", nullable = false, length = 50)
    private String consecutivo; 

    @Column(name = "doc_fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "doc_fecha_vencimiento")
    private LocalDate fechaVencimiento; // Clave para cálculo de mora

    @Column(name = "doc_total", precision = 15, scale = 2)
    private BigDecimal totalDocumento; 

    @Column(name = "doc_saldo", precision = 15, scale = 2)
    private BigDecimal saldoDocumento; 

    @Column(name = "doc_estado", length = 20, nullable = false)
    private String estado; // "EMITIDO", "BORRADOR", "ANULADO", "PAGADO"

    @Column(name = "doc_observaciones", length = 500)
    private String observaciones;

    @Column(name = "doc_metadata_json", columnDefinition = "TEXT")
    private String metadataJson; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_padre_id")
    private Documento documentoPadre; 

    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoDetalle> detalles = new ArrayList<>();

    @Version
    @Column(name = "doc_version")
    private Long version; // Control de concurrencia (Optimistic Locking)

    public void addDetalle(DocumentoDetalle detalle) {
        detalles.add(detalle);
        detalle.setDocumento(this);
    }
}