package com.nodo.inv.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Tercero;

@Repository
public interface TerceroRepository extends JpaRepository<Tercero, Long> {
    boolean existsByDocumento(String documento);
    Optional<Tercero> findByDocumento(String documento);
}
