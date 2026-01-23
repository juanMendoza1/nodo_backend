package com.nodo.inv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nodo.inv.entity.Usuario;
import com.nodo.inv.entity.UsuarioRol;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

List<UsuarioRol> findByUsuario(Usuario usuario);
    
    // 🚀 Cambiamos a LEFT JOIN FETCH para que no sea obligatorio tener permisos
    @Query("SELECT ur FROM UsuarioRol ur "
            + "JOIN FETCH ur.rol r "
            + "LEFT JOIN FETCH r.rolPermisos rp "
            + "LEFT JOIN FETCH rp.permiso "
            + "WHERE ur.usuario = :usuario")
    List<UsuarioRol> findByUsuarioWithRolAndPermisos(@Param("usuario") Usuario usuario);

}
