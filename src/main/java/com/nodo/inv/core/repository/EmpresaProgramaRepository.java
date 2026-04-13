package com.nodo.inv.core.repository;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.EmpresaPrograma;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaProgramaRepository extends JpaRepository<EmpresaPrograma, Long> {
    List<EmpresaPrograma> findByEmpresa(Empresa empresa);
}
