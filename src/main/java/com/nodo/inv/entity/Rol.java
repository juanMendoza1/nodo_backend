package com.nodo.inv.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_ideregistro")
    private Long id;

    @Column(name = "rol_nombre", nullable = false, unique = true, length = 50)
    private String nombre;
    
    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    private List<RolPermiso> rolPermisos;

    // getters y setters
}
