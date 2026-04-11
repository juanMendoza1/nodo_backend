package com.nodo.inv.repository;

import com.nodo.inv.entity.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {
    Optional<Liquidacion> findByCodigo(String codigo);
    List<Liquidacion> findByProgramaId(Long programaId);
    
    @Query("SELECT l FROM Liquidacion l WHERE l.programa IS NULL")
    List<Liquidacion> findTransversales();
}