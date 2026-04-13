package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.core.entity.TerminalDispositivo;

import java.util.List;
import java.util.Optional;

public interface TerminalDispositivoRepository extends JpaRepository<TerminalDispositivo, Long> {
    Optional<TerminalDispositivo> findByUuidHardware(String uuidHardware);
    List<TerminalDispositivo> findBySuscripcionEmpresaId(Long empresaId);
    long countBySuscripcionEmpresaIdAndBloqueadoFalse(Long empresaId);
}