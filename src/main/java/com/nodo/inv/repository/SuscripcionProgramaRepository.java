package com.nodo.inv.repository;

import com.nodo.inv.entity.SuscripcionPrograma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuscripcionProgramaRepository extends JpaRepository<SuscripcionPrograma, Long> {
    Optional<SuscripcionPrograma> findByEmpresaIdAndProgramaCodigo(Long empresaId, String programaCodigo);
}