package com.nodo.inv.repository;

import com.nodo.inv.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    
    // Buscar todas las mesas de una empresa específica
    List<Mesa> findByEmpresaId(Long empresaId);

    // Buscar una mesa específica por su ID local de la tablet y empresa
    Optional<Mesa> findByEmpresaIdAndIdMesaLocal(Long empresaId, Integer idMesaLocal);
    
    // Obtener mesas activas para el monitor en tiempo real
    List<Mesa> findByEmpresaIdAndEstado(Long empresaId, String estado);
}