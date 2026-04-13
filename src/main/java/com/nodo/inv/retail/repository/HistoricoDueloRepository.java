package com.nodo.inv.retail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.retail.entity.HistoricoDuelo;

import java.util.List;

@Repository
public interface HistoricoDueloRepository extends JpaRepository<HistoricoDuelo, Long> {
    
    List<HistoricoDuelo> findByEmpresaIdOrderByFechaFinalizacionDesc(Long empresaId);
    
    boolean existsByUuidDuelo(String uuidDuelo);
}