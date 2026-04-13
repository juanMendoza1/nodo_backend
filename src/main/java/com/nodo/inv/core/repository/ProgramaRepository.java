package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Programa;

import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Long> {
    Optional<Programa> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
