package com.nodo.inv.core.service;

import com.nodo.inv.Utils.EstadoPeriodo;
import com.nodo.inv.Utils.FrecuenciaCiclo;
import com.nodo.inv.core.dto.CicloFacturacionDTO;
import com.nodo.inv.core.entity.CicloFacturacion;
import com.nodo.inv.core.entity.PeriodoFacturacion;
import com.nodo.inv.core.repository.CicloFacturacionRepository;
import com.nodo.inv.core.repository.PeriodoFacturacionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CicloFacturacionService {

    private final CicloFacturacionRepository cicloRepository;
    private final PeriodoFacturacionRepository periodoRepository;

    @Transactional(readOnly = true)
    public List<CicloFacturacion> obtenerTodos() {
        return cicloRepository.findAll();
    }

    @Transactional
    public CicloFacturacion guardarCiclo(CicloFacturacionDTO dto, int anioProyeccion) {
        CicloFacturacion ciclo;
        boolean esNuevo = false;

        if (dto.getId() != null) {
            ciclo = cicloRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Ciclo no encontrado"));
        } else {
            ciclo = new CicloFacturacion();
            esNuevo = true;
        }

        ciclo.setNombre(dto.getNombre());
        ciclo.setFrecuencia(FrecuenciaCiclo.valueOf(dto.getFrecuencia()));
        ciclo.setDiaCorte(dto.getDiaCorte());
        ciclo.setDiasGracia(dto.getDiasGracia());
        ciclo.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        CicloFacturacion cicloGuardado = cicloRepository.save(ciclo);

        // 🔥 Si es un ciclo nuevo, proyectamos automáticamente todo el año
        if (esNuevo) {
            generarProyeccionAnual(cicloGuardado, anioProyeccion);
        }

        return cicloGuardado;
    }

    /**
     * 🔥 MAGIA PURA: Genera los bloques de tiempo (EN_ESPERA) según la frecuencia
     */
    @Transactional
    public void generarProyeccionAnual(CicloFacturacion ciclo, int anio) {
        List<PeriodoFacturacion> periodosNuevos = new ArrayList<>();
        
        int saltoMeses = 1; // Por defecto MENSUAL
        if (ciclo.getFrecuencia() == FrecuenciaCiclo.BIMENSUAL) saltoMeses = 2;
        else if (ciclo.getFrecuencia() == FrecuenciaCiclo.TRIMESTRAL) saltoMeses = 3;
        else if (ciclo.getFrecuencia() == FrecuenciaCiclo.SEMESTRAL) saltoMeses = 6;
        else if (ciclo.getFrecuencia() == FrecuenciaCiclo.ANUAL) saltoMeses = 12;

        for (int mes = 1; mes <= 12; mes += saltoMeses) {
            
            // Evitar duplicados si el administrador manda a proyectar un año que ya existe
            if (periodoRepository.findByCicloIdAndMesOrigenAndAnioOrigen(ciclo.getId(), mes, anio).isPresent()) {
                continue;
            }

            PeriodoFacturacion periodo = new PeriodoFacturacion();
            periodo.setCiclo(ciclo);
            periodo.setAnioOrigen(anio);
            periodo.setMesOrigen(mes);

            LocalDate fechaInicio = LocalDate.of(anio, mes, 1);
            LocalDate fechaFinBloque = fechaInicio.plusMonths(saltoMeses - 1);
            YearMonth yearMonthFin = YearMonth.from(fechaFinBloque);
            
            // O respetamos el día de corte, o nos vamos a fin de mes si febrero no tiene 30 días
            int diaCorteReal = Math.min(ciclo.getDiaCorte(), yearMonthFin.lengthOfMonth());
            LocalDate fechaFinReal = LocalDate.of(yearMonthFin.getYear(), yearMonthFin.getMonth(), diaCorteReal);
            
            periodo.setFechaInicio(fechaInicio);
            periodo.setFechaFin(fechaFinReal);
            periodo.setFechaCorte(fechaFinReal.atTime(23, 59, 59)); 
            periodo.setFechaVencimientoPago(fechaFinReal.plusDays(ciclo.getDiasGracia()));
            periodo.setEstado(EstadoPeriodo.EN_ESPERA);

            periodosNuevos.add(periodo);
        }

        if (!periodosNuevos.isEmpty()) {
            periodoRepository.saveAll(periodosNuevos);
        }
    }

    // ====================================================================
    // MÁQUINA DE ESTADOS (Control Operativo del Periodo)
    // ====================================================================
    
    @Transactional(readOnly = true)
    public List<PeriodoFacturacion> obtenerPeriodosPorCiclo(Long cicloId) {
        return periodoRepository.findByCicloIdOrderByAnioOrigenDescMesOrigenDesc(cicloId);
    }

    @Transactional
    public void abrirPeriodo(Long periodoId) {
        PeriodoFacturacion periodo = periodoRepository.findById(periodoId).orElseThrow();
        
        if (periodo.getEstado() != EstadoPeriodo.EN_ESPERA) {
            throw new RuntimeException("Solo se pueden abrir periodos que están EN_ESPERA.");
        }
        
        // Regla: No puede haber dos periodos abiertos al mismo tiempo para el mismo ciclo
        List<PeriodoFacturacion> activos = periodoRepository.findByCicloIdAndEstadoIn(
                periodo.getCiclo().getId(), 
                List.of(EstadoPeriodo.ABIERTO, EstadoPeriodo.LIQUIDANDO)
        );
        
        if (!activos.isEmpty()) {
            throw new RuntimeException("Ya existe un periodo Abierto o en Proceso para este ciclo. Ciérrelo primero.");
        }

        periodo.setEstado(EstadoPeriodo.ABIERTO);
        periodoRepository.save(periodo);
    }

    @Transactional
    public void iniciarProcesoLiquidacion(Long periodoId) {
        PeriodoFacturacion periodo = periodoRepository.findById(periodoId).orElseThrow();
        if (periodo.getEstado() != EstadoPeriodo.ABIERTO) {
            throw new RuntimeException("El periodo debe estar ABIERTO para poder procesar la facturación.");
        }
        periodo.setEstado(EstadoPeriodo.LIQUIDANDO);
        periodoRepository.save(periodo);
    }

    @Transactional
    public void cerrarPeriodo(Long periodoId) {
        PeriodoFacturacion periodo = periodoRepository.findById(periodoId).orElseThrow();
        if (periodo.getEstado() != EstadoPeriodo.ABIERTO && periodo.getEstado() != EstadoPeriodo.LIQUIDANDO) {
            throw new RuntimeException("El periodo debe estar ABIERTO para poder cerrarse definitivamente.");
        }
        periodo.setEstado(EstadoPeriodo.CERRADO);
        periodoRepository.save(periodo);
    }
}