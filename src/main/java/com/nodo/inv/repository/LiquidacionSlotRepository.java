package com.nodo.inv.repository;

import com.nodo.inv.entity.LiquidacionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiquidacionSlotRepository extends JpaRepository<LiquidacionSlot, Long> {
    // Historial de pagos de una empresa
    List<LiquidacionSlot> findByEmpresaIdOrderByFechaGeneracionDesc(Long empresaId);
    
    // Historial de pagos de un empleado en específico
    List<LiquidacionSlot> findByUsuarioSlotIdOrderByFechaGeneracionDesc(Long usuarioSlotId);
}