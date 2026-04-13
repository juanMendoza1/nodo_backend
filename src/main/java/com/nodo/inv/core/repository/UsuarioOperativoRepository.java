package com.nodo.inv.core.repository;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.core.entity.UsuarioOperativo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioOperativoRepository extends JpaRepository<UsuarioOperativo, Long> {
    List<UsuarioOperativo> findByEmpresaId(Long empresaId);
    Optional<UsuarioOperativo> findByLogin(String login);
    long countByEmpresaIdAndEstado(Long empresaId, EstadoUsuario estado);
}