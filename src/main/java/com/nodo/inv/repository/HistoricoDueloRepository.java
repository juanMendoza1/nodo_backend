package com.nodo.inv.repository;

import com.nodo.inv.entity.HistoricoDuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoDueloRepository extends JpaRepository<HistoricoDuelo, Long> {
    
    List<HistoricoDuelo> findByEmpresaIdOrderByFechaFinalizacionDesc(Long empresaId);
    
    boolean existsByUuidDuelo(String uuidDuelo);
}