package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Estructura;

import java.util.Optional;

@Repository
public interface EstructuraRepository extends JpaRepository<Estructura, Long> {
    // Método necesario para DataInitializer
    Optional<Estructura> findByCodigo(String codigo);
}