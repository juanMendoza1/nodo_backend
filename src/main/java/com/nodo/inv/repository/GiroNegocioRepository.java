package com.nodo.inv.repository;

import com.nodo.inv.entity.GiroNegocio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiroNegocioRepository extends JpaRepository<GiroNegocio, Long> {
    Optional<GiroNegocio> findByCodigo(String codigo);
}