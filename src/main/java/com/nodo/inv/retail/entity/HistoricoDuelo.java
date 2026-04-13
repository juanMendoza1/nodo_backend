package com.nodo.inv.retail.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.nodo.inv.core.entity.Empresa;

@Entity
@Table(name = "historico_duelos")
@Data
public class HistoricoDuelo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuidDuelo;
    private Integer idMesa;
    private String tipoJuego;
    
    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    // Guardamos el JSON completo del detalle para auditoría profunda
    @Column(columnDefinition = "TEXT")
    private String detalleJson;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}