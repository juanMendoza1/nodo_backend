package com.nodo.inv.repository;

import com.nodo.inv.entity.Producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
	List<Producto> findByEmpresaIdAndActivoTrue(Long empresaId);
}