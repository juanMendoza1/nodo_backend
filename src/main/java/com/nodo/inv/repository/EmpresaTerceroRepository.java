package com.nodo.inv.repository;

import com.nodo.inv.entity.EmpresaTercero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmpresaTerceroRepository extends JpaRepository<EmpresaTercero, Long> {

    // La consulta clave: Traer los que pertenecen a mi empresa O los que Juan marcó como globales
    @Query("SELECT et FROM EmpresaTercero et " +
           "JOIN FETCH et.tercero t " +
           "WHERE et.empresa.id = :empresaId OR et.esGlobal = true")
    List<EmpresaTercero> findVisibleByEmpresa(@Param("empresaId") Long empresaId);
}
