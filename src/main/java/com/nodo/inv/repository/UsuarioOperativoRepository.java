package com.nodo.inv.repository;

import com.nodo.inv.entity.UsuarioOperativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioOperativoRepository extends JpaRepository<UsuarioOperativo, Long> {
    List<UsuarioOperativo> findByEmpresaId(Long empresaId);
    Optional<UsuarioOperativo> findByLogin(String login);
}