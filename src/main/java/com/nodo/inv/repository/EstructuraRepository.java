package com.nodo.inv.repository;

import com.nodo.inv.entity.Estructura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstructuraRepository extends JpaRepository<Estructura, Long> {
    // Método necesario para DataInitializer
    Optional<Estructura> findByCodigo(String codigo);
}