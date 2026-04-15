package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nodo.inv.core.entity.CicloFacturacion;

import java.util.List;

@Repository
public interface CicloFacturacionRepository extends JpaRepository<CicloFacturacion, Long> {
    List<CicloFacturacion> findByActivoTrue();
}