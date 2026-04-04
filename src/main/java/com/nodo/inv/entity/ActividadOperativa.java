package com.nodo.inv.entity;

import jakarta.persistence.*; // Si usas Spring Boot 2.x, cambia 'jakarta' por 'javax'
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "actividad_operativa")
public class ActividadOperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActividad;

    // El UUID único generado por la App para esta acción. 
    // unique = true asegura que si la app reintenta enviar, la BD rechace el duplicado.
    @Column(name = "evento_id", nullable = false, unique = true)
    private String eventoId; 

    @Column(name = "terminal_uuid", nullable = false)
    private String terminalUuid; // Para saber qué tablet generó la acción

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento; // Ej: "PEDIDO", "DESPACHO", "CIERRE", "DUELO"

    // Crucial para el asincronismo: "PENDIENTE", "PROCESADO", "ERROR"
    @Column(name = "estado_procesamiento", nullable = false)
    private String estadoProcesamiento; 

    @Column(name = "fecha_dispositivo", nullable = false)
    private Long fechaDispositivo; // Timestamp de cuando ocurrió realmente en el local

    @Column(name = "fecha_servidor")
    private LocalDateTime fechaServidor; // Cuándo llegó este dato al backend

    // Usamos TEXT para guardar el JSON de forma flexible sin complicar dependencias de Hibernate
    @Column(name = "detalles_json", columnDefinition = "TEXT")
    private String detallesJson; 

    // Aislamiento SaaS: Siempre atado a una empresa específica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;
}