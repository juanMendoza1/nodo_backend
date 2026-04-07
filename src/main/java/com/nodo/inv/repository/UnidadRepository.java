package com.nodo.inv.repository;

import com.nodo.inv.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    // Busca unidades por el ID de la estructura (ej: Categorías)
    List<Unidad> findByEstructuraId(Long estructuraId);
    
    // Busca por el código de la estructura para que sea más fácil de usar
    List<Unidad> findByEstructuraCodigo(String codigoEstructura);
    
    Optional<Unidad> findByCodigo(String codigo);
    
    @Query("SELECT u FROM Unidad u WHERE u.estructura.codigo = :codigoEstructura AND (u.esGlobal = true OR u.empresa.id = :empresaId)")
    List<Unidad> findByEstructuraCodigoAndEmpresa(
            @Param("codigoEstructura") String codigoEstructura, 
            @Param("empresaId") Long empresaId);
}