package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodo.inv.Utils.EstadoUsuario;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ter_ideregistro", nullable = false)
    private Tercero tercero;

    @Column(name = "usu_login", nullable = false, unique = true, length = 100)
    private String login;

    @Enumerated(EnumType.STRING)
    @Column(name = "usu_estado", nullable = false)
    private EstadoUsuario estado;

    @Column(name = "usu_passw", nullable = false)
    private String password;

    @Column(name = "usu_fechaactivacion")
    private LocalDateTime fechaActivacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    // getters y setters
}