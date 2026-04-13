package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.DocumentoDetalle;

import java.util.List;

@Repository
public interface DocumentoDetalleRepository extends JpaRepository<DocumentoDetalle, Long> {
    List<DocumentoDetalle> findByDocumentoId(Long documentoId);
}