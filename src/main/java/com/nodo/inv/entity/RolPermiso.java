package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter
@NoArgsConstructor // Recomendado para JPA
@AllArgsConstructor // Útil para tus propios mapeos
@Entity
@Table(
    name = "rol_permiso",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rol_ideregistro", "per_ideregistro"})
    }
)
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rp_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_ideregistro", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "per_ideregistro", nullable = false)
    private Permiso permiso;
}