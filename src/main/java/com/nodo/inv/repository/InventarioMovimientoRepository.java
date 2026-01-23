package com.nodo.inv.repository;

import com.nodo.inv.entity.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {
    List<InventarioMovimiento> findByEmpresaId(Long empresaId);
    List<InventarioMovimiento> findByProductoId(Long productoId);
}