package com.nodo.inv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);
    List<Usuario> findByEmpresaIdAndEstado(Long empresaId, EstadoUsuario estado);
}
