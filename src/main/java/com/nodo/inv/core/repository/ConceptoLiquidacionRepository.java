package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.ConceptoLiquidacion;

import java.util.List;

@Repository
public interface ConceptoLiquidacionRepository extends JpaRepository<ConceptoLiquidacion, Long> {
    
	@Query("SELECT cl FROM ConceptoLiquidacion cl JOIN FETCH cl.concepto c " +
	           "WHERE cl.liquidacion.codigo = :codigoLiquidacion " +
	           "AND cl.empresa.id = :empresaId " +
	           "AND ((:programaId = 0 AND cl.programa IS NULL) OR cl.programa.id = :programaId) " +
	           "ORDER BY cl.ordenCalculo ASC")
	    List<ConceptoLiquidacion> obtenerRecetaDeLiquidacion(
	            @Param("codigoLiquidacion") String codigoLiquidacion,
	            @Param("empresaId") Long empresaId,
	            @Param("programaId") Long programaId);
}