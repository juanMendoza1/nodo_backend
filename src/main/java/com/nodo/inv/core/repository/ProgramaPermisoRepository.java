package com.nodo.inv.core.repository;

import com.nodo.inv.core.entity.Programa;
import com.nodo.inv.core.entity.ProgramaPermiso;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgramaPermisoRepository extends JpaRepository<ProgramaPermiso, Long> {
    List<ProgramaPermiso> findByPrograma(Programa programa);
    void deleteByPrograma(Programa programa);
}