package com.nodo.inv.repository;

import com.nodo.inv.entity.Duelo;
import com.nodo.inv.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DueloRepository extends JpaRepository<Duelo, Long> {
    Optional<Duelo> findByUuidDuelo(String uuidDuelo);
    // Para encontrar el juego activo de una mesa cuando se vaya a cerrar
    Optional<Duelo> findByMesaAndEstado(Mesa mesa, String estado); 
}