// src/main/java/com/nodo/inv/entity/Rol.java
package com.nodo.inv.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    @Column(name = "rol_descripcion", length = 200)
    private String descripcion;
    
    @Column(name = "rol_activo")
    private Boolean activo = true;
    
    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    @JsonIgnore 
    private List<RolPermiso> rolPermisos;
}