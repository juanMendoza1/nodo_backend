package com.nodo.inv.repository;

import com.nodo.inv.entity.Tercero;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerceroRepository extends JpaRepository<Tercero, Long> {
    boolean existsByDocumento(String documento);
    Optional<Tercero> findByDocumento(String documento);
}
