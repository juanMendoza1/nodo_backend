package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Estructura;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstructuraRepository extends JpaRepository<Estructura, Long> {
    // Método necesario para DataInitializer
    Optional<Estructura> findByCodigo(String codigo);
    
    @Query("SELECT DISTINCT pe.estructura FROM ProgramaEstructura pe " +
            "JOIN SuscripcionPrograma sp ON pe.programa.id = sp.programa.id " +
            "WHERE sp.empresa.id = :empresaId AND sp.activo = true")
     List<Estructura> findPermitidasPorEmpresa(@org.springframework.data.repository.query.Param("empresaId") Long empresaId);
}