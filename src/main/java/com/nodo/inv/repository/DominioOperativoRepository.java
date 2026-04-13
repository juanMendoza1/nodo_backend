package com.nodo.inv.repository;

import com.nodo.inv.entity.DominioOperativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DominioOperativoRepository extends JpaRepository<DominioOperativo, Long> {
    Optional<DominioOperativo> findByCodigo(String codigo);
}