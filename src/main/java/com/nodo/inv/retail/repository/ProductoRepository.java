package com.nodo.inv.retail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nodo.inv.retail.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
	List<Producto> findByEmpresaIdAndActivoTrue(Long empresaId);
	List<Producto> findByEmpresaId(Long empresaId);
	
	long countByEmpresaIdAndActivoTrue(Long empresaId);
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.empresa.id = :empresaId AND p.activo = true AND p.stockActual <= p.stockMinimo")
    long countProductosBajoStock(@org.springframework.data.repository.query.Param("empresaId") Long empresaId);
}