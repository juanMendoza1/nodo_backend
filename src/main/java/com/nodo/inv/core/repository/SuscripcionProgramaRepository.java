package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nodo.inv.core.entity.SuscripcionPrograma;
import java.util.List;
import java.util.Optional;

public interface SuscripcionProgramaRepository extends JpaRepository<SuscripcionPrograma, Long> {
    Optional<SuscripcionPrograma> findByEmpresaIdAndProgramaCodigo(Long empresaId, String programaCodigo);
    List<SuscripcionPrograma> findByEmpresaIdAndActivoTrue(Long empresaId);
    
    // 🔥 NUEVA CONSULTA PARA EL BATCH BILLING (Solo los activos)
    List<SuscripcionPrograma> findByCicloFacturacionIdAndActivoTrue(Long cicloId);
}