package com.nodo.inv.repository;

import com.nodo.inv.entity.TerminalTokenRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TerminalTokenRepository extends JpaRepository<TerminalTokenRegistro, Long> {
    Optional<TerminalTokenRegistro> findByTokenAndUsadoFalse(String token);
}