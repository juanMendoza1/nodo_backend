package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "inv_terminal_dispositivo")
public class TerminalDispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sp_ideregistro", nullable = false)
    @JsonIgnore
    private SuscripcionPrograma suscripcion;

    // --- IDENTIFICACIÓN TÉCNICA Y SEGURIDAD ---
    @Column(name = "td_uuid_hardware", nullable = false, unique = true)
    private String uuidHardware; // Identificador interno (Android ID / MAC)

    @Column(name = "td_serial_fabricante", unique = true)
    private String serial; // Serial físico del equipo

    @Column(name = "td_marca")
    private String marca;

    @Column(name = "td_modelo")
    private String modelo;

    @Column(name = "td_alias")
    private String alias; // Ej: "Tablet Barra Principal" o "Tablet Terraza"

    // --- CONTROL OPERATIVO ---
    @Column(name = "td_fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "td_ultimo_acceso")
    private LocalDateTime ultimoAcceso; // Para auditoría de conexión

    @Column(name = "td_bloqueado")
    private Boolean bloqueado = false; // Bloqueo remoto preventivo

    @Column(name = "td_estado_fisico")
    private String estadoFisico; // Ej: "NUEVO", "BUENO", "EN REPARACIÓN"
}