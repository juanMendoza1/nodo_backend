package com.nodo.inv.repository;

import com.nodo.inv.entity.Concepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptoRepository extends JpaRepository<Concepto, Long> {
    
    Optional<Concepto> findByCodigo(String codigo);

    // 🔥 CORRECCIÓN APLICADA: 
    // Trae (Los globales de ese programa) + (Los que el cliente creó en su empresa para ese programa)
    @Query("SELECT c FROM Concepto c WHERE c.activo = true " +
           "AND c.programa.id = :programaId " +
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