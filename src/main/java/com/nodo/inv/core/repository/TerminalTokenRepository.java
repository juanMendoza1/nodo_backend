package com.nodo.inv.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.core.entity.TerminalTokenRegistro;

import java.util.Optional;

public interface TerminalTokenRepository extends JpaRepository<TerminalTokenRegistro, Long> {
    Optional<TerminalTokenRegistro> findByTokenAndUsadoFalse(String token);
}