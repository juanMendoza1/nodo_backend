package com.nodo.inv.repository;

import com.nodo.inv.entity.TerminalDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TerminalDispositivoRepository extends JpaRepository<TerminalDispositivo, Long> {
    Optional<TerminalDispositivo> findByUuidHardware(String uuidHardware);
    List<TerminalDispositivo> findBySuscripcionEmpresaId(Long empresaId);
    long countBySuscripcionEmpresaIdAndBloqueadoFalse(Long empresaId);
}