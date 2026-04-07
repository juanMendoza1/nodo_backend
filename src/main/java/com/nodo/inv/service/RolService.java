// src/main/java/com/nodo/inv/service/RolService.java
package com.nodo.inv.service;

import com.nodo.inv.dto.RolDTO;
import com.nodo.inv.entity.Rol;
import com.nodo.inv.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> obtenerTodos() {
        return rolRepository.findAll();
    }

    @Transactional
    public Rol guardar(RolDTO dto) {
        Rol rol;
        if (dto.getId() != null) {
            rol = rolRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        } else {
            rol = new Rol();
        }

        // Forzamos mayúsculas para estandarizar los roles
        rol.setNombre(dto.getNombre().toUpperCase()); 
        rol.setDescripcion(dto.getDescripcion());
        rol.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return rolRepository.save(rol);
    }

    @Transactional
    public void eliminar(Long id) {
        try {
            rolRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar el rol porque está asignado a uno o más usuarios.");
        }
    }
}