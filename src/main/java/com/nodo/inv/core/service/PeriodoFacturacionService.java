package com.nodo.inv.core.service;

import com.nodo.inv.Utils.EstadoPeriodo;
import com.nodo.inv.core.dto.PeriodoFacturacionDTO;
import com.nodo.inv.core.entity.PeriodoFacturacion;
import com.nodo.inv.core.repository.PeriodoFacturacionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeriodoFacturacionService {

    private final PeriodoFacturacionRepository periodoRepository;

    @Transactional(readOnly = true)
    public PeriodoFacturacion obtenerPorId(Long id) {
        return periodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periodo de facturación no encontrado."));
    }

    @Transactional
    public PeriodoFacturacion actualizarPeriodo(Long id, PeriodoFacturacionDTO dto) {
        PeriodoFacturacion periodo = periodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periodo de facturación no encontrado."));

        // 🔥 BLINDAJE CONTABLE: No dejamos tocar lo que ya se facturó
        if (periodo.getEstado() == EstadoPeriodo.CERRADO || periodo.getEstado() == EstadoPeriodo.LIQUIDANDO) {
            throw new RuntimeException("No se puede modificar un periodo que se encuentra en estado " + periodo.getEstado());
        }

        // Actualizamos las fechas operativas (La famosa "gabela" o ajuste de fin de mes)
        if (dto.getFechaInicio() != null) periodo.setFechaInicio(dto.getFechaInicio());
        if (dto.getFechaFin() != null) periodo.setFechaFin(dto.getFechaFin());
        if (dto.getFechaCorte() != null) periodo.setFechaCorte(dto.getFechaCorte());
        if (dto.getFechaVencimientoPago() != null) periodo.setFechaVencimientoPago(dto.getFechaVencimientoPago());

        return periodoRepository.save(periodo);
    }
}