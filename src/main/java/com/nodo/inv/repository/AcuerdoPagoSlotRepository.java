package com.nodo.inv.repository;

import com.nodo.inv.entity.AcuerdoPagoSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcuerdoPagoSlotRepository extends JpaRepository<AcuerdoPagoSlot, Long> {
    
    Optional<AcuerdoPagoSlot> findByUsuarioSlotIdAndEstado(Long usuarioSlotId, String estado);
    
    List<AcuerdoPagoSlot> findByUsuarioSlotIdAndEstadoNotOrderByFechaCreacionDesc(Long usuarioSlotId, String estado);
    
}