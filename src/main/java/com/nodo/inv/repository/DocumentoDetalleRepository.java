package com.nodo.inv.repository;

import com.nodo.inv.entity.DocumentoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoDetalleRepository extends JpaRepository<DocumentoDetalle, Long> {
    List<DocumentoDetalle> findByDocumentoId(Long documentoId);
}