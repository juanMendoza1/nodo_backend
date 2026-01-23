package com.nodo.inv.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
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

    @Column(name = "ter_sexo", length = 10)
    private String sexo;

    @Column(name = "ter_telefono", length = 20)
    private String telefono;

    @Column(name = "ter_correo", length = 150)
    private String correo;

    @Column(name = "est_tiptercero")
    private Integer estadoTipoTercero;

    @Column(name = "uni_tiptercero")
    private Integer tipoTercero;

    @Column(name = "uni_tipidentifica")
    private Integer tipoIdentificacion;

    @Column(name = "ter_fecnacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "ter_docexpedicion")
    private LocalDate fechaExpedicionDocumento;

    @Column(name = "ter_fechaautenticacion")
    private LocalDateTime fechaAutenticacion;

    // getters y setters
}