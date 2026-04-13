package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.TipoDocumento;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    Optional<TipoDocumento> findByCodigo(String codigo);
    List<TipoDocumento> findByActivoTrue();
}