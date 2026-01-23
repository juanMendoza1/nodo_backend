package com.nodo.inv.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
    name = "usuario_rol",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usu_ideregistro", "rol_ideregistro"})
    }
)
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usr_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_ideregistro", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_ideregistro", nullable = false)
    private Rol rol;

    @Column(name = "usr_fechacreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "aud_ideregistro")
    private Long auditoriaId;

    // getters y setters
}