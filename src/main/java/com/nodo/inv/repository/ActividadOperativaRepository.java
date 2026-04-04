package com.nodo.inv.repository;

import com.nodo.inv.entity.ActividadOperativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadOperativaRepository extends JpaRepository<ActividadOperativa, Long> {
    
    // Este método es oro puro: nos dirá si la tablet ya había enviado esta acción
    boolean existsByEventoId(String eventoId);
}