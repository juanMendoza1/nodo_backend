package com.nodo.inv.core.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore; // 🔥 IMPORTANTE: Asegúrate de importar esto

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_ideregistro")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ter_ideregistro", nullable = false)
    private Tercero tercero;

    @Column(name = "emp_nombre_comercial", nullable = false, length = 200)
    private String nombreComercial;

    @Column(name = "emp_estado")
    private Boolean activo;

    // 🔥 SOLUCIÓN: Ignoramos las listas para romper el bucle infinito
    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;
    
    // 🔥 SOLUCIÓN: Ignoramos los programas contratados para que no llame de vuelta a la empresa
    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<EmpresaPrograma> programasContratados;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gn_ideregistro")
    private GiroNegocio giroNegocio;
}