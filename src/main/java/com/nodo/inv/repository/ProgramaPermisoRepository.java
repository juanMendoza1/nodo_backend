package com.nodo.inv.repository;

import com.nodo.inv.entity.Programa;
import com.nodo.inv.entity.ProgramaPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgramaPermisoRepository extends JpaRepository<ProgramaPermiso, Long> {
    List<ProgramaPermiso> findByPrograma(Programa programa);
    void deleteByPrograma(Programa programa);
}