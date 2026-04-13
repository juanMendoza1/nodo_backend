package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.ConceptoRelacionado;

import java.util.List;

@Repository
public interface ConceptoRelacionadoRepository extends JpaRepository<ConceptoRelacionado, Long> {
    
    // Si pregunto por el concepto "IVA", me trae la lista de los conceptos a los que les debe sacar el porcentaje
    List<ConceptoRelacionado> findByConceptoPadreId(Long conceptoPadreId);
    
    // Validar si un concepto está siendo usado por otro antes de dejar que el Admin lo borre
    boolean existsByConceptoHijoId(Long conceptoHijoId);
}