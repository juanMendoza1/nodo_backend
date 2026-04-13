package com.nodo.inv.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.core.entity.Rol;
import com.nodo.inv.core.entity.RolPermiso;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    List<RolPermiso> findByRol(Rol rol);
}
