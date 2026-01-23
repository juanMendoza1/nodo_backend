package com.nodo.inv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.entity.Rol;
import com.nodo.inv.entity.RolPermiso;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    List<RolPermiso> findByRol(Rol rol);
}
