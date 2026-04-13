package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Clase;

import java.util.Optional;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    // Método necesario para DataInitializer
    Optional<Clase> findByCodigo(String codigo);
}