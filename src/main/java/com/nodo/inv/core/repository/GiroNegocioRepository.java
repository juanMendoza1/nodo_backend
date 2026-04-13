package com.nodo.inv.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.GiroNegocio;

@Repository
public interface GiroNegocioRepository extends JpaRepository<GiroNegocio, Long> {
    Optional<GiroNegocio> findByCodigo(String codigo);
}