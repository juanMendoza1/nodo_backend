package com.nodo.inv.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
	List<Empresa> findByTerceroId(Long terceroId);
}
