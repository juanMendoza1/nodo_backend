package com.nodo.inv.repository;

import com.nodo.inv.entity.ConceptoLiquidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptoLiquidacionRepository extends JpaRepository<ConceptoLiquidacion, Long> {
    
    // 🔥 LA CONSULTA ESTRELLA: 
    // "Tráeme la receta exacta que configuró esta Empresa, para esta Plantilla, ordenada por paso de cálculo"
    @Query("SELECT cl FROM ConceptoLiquidacion cl " +
           "JOIN FETCH cl.concepto c " +
           "WHERE cl.liquidacion.codigo = :codigoLiquidacion " +
           "AND cl.empresa.id = :empresaId " +
           "AND cl.programa.id = :programaId " +
           "ORDER BY cl.ordenCalculo ASC")
    List<ConceptoLiquidacion> obtenerRecetaDeLiquidacion(
            @Param("codigoLiquidacion") String codigoLiquidacion,
            @Param("empresaId") Long empresaId,
            @Param("programaId") Long programaId);
}