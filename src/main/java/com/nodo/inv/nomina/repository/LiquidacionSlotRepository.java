package com.nodo.inv.nomina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.nomina.entity.LiquidacionSlot;

import java.util.List;

@Repository
public interface LiquidacionSlotRepository extends JpaRepository<LiquidacionSlot, Long> {
    // Historial de pagos de una empresa
    List<LiquidacionSlot> findByEmpresaIdOrderByFechaGeneracionDesc(Long empresaId);
    
    // Historial de pagos de un empleado en específico
    List<LiquidacionSlot> findByUsuarioSlotIdOrderByFechaGeneracionDesc(Long usuarioSlotId);
}