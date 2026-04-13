// src/main/java/com/nodo/inv/entity/Permiso.java
package com.nodo.inv.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "per_ideregistro")
    private Long id;

    @Column(name = "per_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "per_descripcion", length = 200)
    private String descripcion;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "modulo_dependencia",
        joinColumns = @JoinColumn(name = "modulo_id"),
        inverseJoinColumns = @JoinColumn(name = "dependencia_id")
    )
    private Set<Permiso> dependencias = new HashSet<>();
}