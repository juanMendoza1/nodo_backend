package com.nodo.inv.nomina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nodo.inv.nomina.entity.Liquidacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {
    Optional<Liquidacion> findByCodigo(String codigo);
    List<Liquidacion> findByProgramaId(Long programaId);
    
    @Query("SELECT l FROM Liquidacion l WHERE l.programa IS NULL")
    List<Liquidacion> findTransversales();
}