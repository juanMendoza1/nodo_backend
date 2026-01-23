package com.nodo.inv.repository;

import com.nodo.inv.entity.EmpresaPrograma;
import com.nodo.inv.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaProgramaRepository extends JpaRepository<EmpresaPrograma, Long> {
    List<EmpresaPrograma> findByEmpresa(Empresa empresa);
}
