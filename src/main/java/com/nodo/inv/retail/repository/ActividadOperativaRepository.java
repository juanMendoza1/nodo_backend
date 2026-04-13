package com.nodo.inv.retail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nodo.inv.retail.entity.ActividadOperativa;

@Repository
public interface ActividadOperativaRepository extends JpaRepository<ActividadOperativa, Long> {
    
    // Este método es oro puro: nos dirá si la tablet ya había enviado esta acción
    boolean existsByEventoId(String eventoId);
    @Query("SELECT a FROM ActividadOperativa a WHERE a.empresa.id = :empresaId AND a.mesa.idMesaLocal = :idMesaLocal ORDER BY a.fechaDispositivo DESC")
    List<ActividadOperativa> findByEmpresaAndMesaLocal(@Param("empresaId") Long empresaId, @Param("idMesaLocal") Integer idMesaLocal);
    @Query("SELECT a FROM ActividadOperativa a WHERE a.empresa.id = :empresaId ORDER BY a.fechaDispositivo DESC")
    List<ActividadOperativa> findByEmpresaIdOrderByFechaDispositivoDesc(@Param("empresaId") Long empresaId);
}