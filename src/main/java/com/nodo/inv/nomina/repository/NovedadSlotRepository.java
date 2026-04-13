package com.nodo.inv.nomina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.nomina.entity.NovedadSlot;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NovedadSlotRepository extends JpaRepository<NovedadSlot, Long> {
    // 🔥 CAMBIO: Buscar notas pendientes pero por CONTRATO
    List<NovedadSlot> findByAcuerdoPagoIdAndAplicadaFalse(Long acuerdoPagoId);
}