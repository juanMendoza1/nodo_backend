package com.nodo.inv.repository;

import com.nodo.inv.entity.NovedadSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NovedadSlotRepository extends JpaRepository<NovedadSlot, Long> {
    // Trae las novedades (Descuentos/Bonos) que aún NO se han cobrado/pagado
    List<NovedadSlot> findByUsuarioSlotIdAndAplicadaFalseAndFechaRegistroBefore(Long usuarioSlotId, LocalDateTime fechaLimite);
}