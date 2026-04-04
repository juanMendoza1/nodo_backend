package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "actividad_operativa")
public class ActividadOperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActividad;

    @Column(name = "evento_id", nullable = false, unique = true)
    private String eventoId; 

    @Column(name = "terminal_uuid", nullable = false)
    private String terminalUuid; 

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento; 

    @Column(name = "estado_procesamiento", nullable = false)
    private String estadoProcesamiento; 

    @Column(name = "fecha_dispositivo", nullable = false)
    private Long fechaDispositivo; 

    @Column(name = "fecha_servidor")
    private LocalDateTime fechaServidor; 

    @Column(name = "detalles_json", columnDefinition = "TEXT")
    private String detallesJson; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mes_ideregistro") // El campo en la tabla será mes_ideregistro
    private Mesa mesa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "due_ideregistro")
    private Duelo duelo;
}