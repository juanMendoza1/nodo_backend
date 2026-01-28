package com.nodo.inv.repository;

import com.nodo.inv.entity.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    // Método necesario para DataInitializer
    Optional<Clase> findByCodigo(String codigo);
}