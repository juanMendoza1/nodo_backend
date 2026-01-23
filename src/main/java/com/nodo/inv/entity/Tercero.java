package com.nodo.inv.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tercero")
public class Tercero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ter_ideregistro")
    private Long id;

    @Column(name = "ter_documento", nullable = false, unique = true, length = 30)
    private String documento;

    @Column(name = "ter_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ter_apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "ter_nomcompleto", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(name = "ter_telefono", length = 20)
    private String telefono;

    @Column(name = "ter_correo", length = 150)
    private String correo;

    @Column(name = "ter_fecnacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "ter_docexpedicion")
    private LocalDate fechaExpedicionDocumento;

    @Column(name = "ter_fechaautenticacion")
    private LocalDateTime fechaAutenticacion;

    // --- RELACIONES CON EL MOTOR DE PARAMETRIZACIÓN ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_sexo") // Nueva relación para sexo dinámico
    private Unidad sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_tiptercero") // Antes era Integer, ahora apunta a UNIDAD
    private Unidad tipoTercero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_tipidentifica") // Antes era Integer, ahora apunta a UNIDAD
    private Unidad tipoIdentificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "est_tiptercero") // Apunta a la ESTRUCTURA para clasificar el tipo
    private Estructura estadoTipoTercero;

}