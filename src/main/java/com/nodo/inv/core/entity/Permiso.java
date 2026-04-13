package com.nodo.inv.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "per_ideregistro")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "per_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "per_descripcion", length = 200)
    private String descripcion;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "modulo_dependencia",
        joinColumns = @JoinColumn(name = "modulo_id"),
        inverseJoinColumns = @JoinColumn(name = "dependencia_id")
    )
    private Set<Permiso> dependencias = new HashSet<>();

    @JsonProperty("dependenciasIds")
    public List<Long> getDependenciasIds() {
        return dependencias.stream().map(Permiso::getId).collect(Collectors.toList());
    }
}