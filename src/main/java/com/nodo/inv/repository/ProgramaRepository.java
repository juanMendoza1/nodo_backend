package com.nodo.inv.repository;

import com.nodo.inv.entity.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Long> {
    Optional<Programa> findByCodigo(String codigo);
}
