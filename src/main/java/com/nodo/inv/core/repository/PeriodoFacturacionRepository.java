package com.nodo.inv.core.repository;

import com.nodo.inv.Utils.EstadoPeriodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nodo.inv.core.entity.PeriodoFacturacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodoFacturacionRepository extends JpaRepository<PeriodoFacturacion, Long> {
    
    // Obtener todos los periodos de un ciclo ordenados cronológicamente
    List<PeriodoFacturacion> findByCicloIdOrderByAnioOrigenDescMesOrigenDesc(Long cicloId);

    // Saber si hay algún periodo ABIERTO o LIQUIDANDO para un ciclo específico (para evitar abrir dos al mismo tiempo)
    List<PeriodoFacturacion> findByCicloIdAndEstadoIn(Long cicloId, List<EstadoPeriodo> estados);
    
    // Buscar un periodo exacto por mes y año
    Optional<PeriodoFacturacion> findByCicloIdAndMesOrigenAndAnioOrigen(Long cicloId, Integer mesOrigen, Integer anioOrigen);
}