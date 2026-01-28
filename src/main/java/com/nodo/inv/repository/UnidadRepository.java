package com.nodo.inv.repository;

import com.nodo.inv.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    // Busca unidades por el ID de la estructura (ej: Categorías)
    List<Unidad> findByEstructuraId(Long estructuraId);
    
    // Busca por el código de la estructura para que sea más fácil de usar
    List<Unidad> findByEstructuraCodigo(String codigoEstructura);
}