package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.DominioOperativo;

import java.util.Optional;

@Repository
public interface DominioOperativoRepository extends JpaRepository<DominioOperativo, Long> {
    Optional<DominioOperativo> findByCodigo(String codigo);
}