package com.nodo.inv.retail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.retail.entity.InventarioMovimiento;

import java.util.List;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {
    List<InventarioMovimiento> findByEmpresaId(Long empresaId);
    List<InventarioMovimiento> findByProductoId(Long productoId);
    List<InventarioMovimiento> findByEmpresaIdOrderByFechaDesc(Long empresaId);
}