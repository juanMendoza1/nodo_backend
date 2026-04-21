package com.nodo.inv.core.repository;

import com.nodo.inv.core.entity.Programa;
import com.nodo.inv.core.entity.ProgramaEstructura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgramaEstructuraRepository extends JpaRepository<ProgramaEstructura, Long> {
    List<ProgramaEstructura> findByPrograma(Programa programa);
    void deleteByPrograma(Programa programa);
}