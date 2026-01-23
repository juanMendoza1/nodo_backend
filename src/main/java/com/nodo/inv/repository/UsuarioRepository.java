package com.nodo.inv.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);
}
