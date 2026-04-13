package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Concepto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptoRepository extends JpaRepository<Concepto, Long> {
    
    Optional<Concepto> findByCodigo(String codigo);

    @Query("SELECT c FROM Concepto c WHERE c.activo = true " +
            "AND ((:programaId = 0 AND c.programa IS NULL) OR c.programa.id = :programaId) " +
            "AND (c.esGlobal = true OR c.empresa.id = :empresaId)")
     List<Concepto> findDisponiblesPorEmpresaYPrograma(
             @Param("empresaId") Long empresaId, 
             @Param("programaId") Long programaId);

    // Búsqueda rápida por etiqueta (Estructura) asegurando Empresa y Programa
    @Query("SELECT c FROM Concepto c WHERE c.estructuraAgrupadora.codigo = :codigoEstructura " +
           "AND c.programa.id = :programaId " +
           "AND (c.esGlobal = true OR c.empresa.id = :empresaId)")
    List<Concepto> findByEstructuraEmpresaYPrograma(
            @Param("codigoEstructura") String codigoEstructura, 
            @Param("empresaId") Long empresaId,
            @Param("programaId") Long programaId);
}