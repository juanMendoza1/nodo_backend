package com.nodo.inv.core.service;

import com.nodo.inv.core.dto.EstructuraDTO;
import com.nodo.inv.core.entity.Clase;
import com.nodo.inv.core.entity.Estructura;
import com.nodo.inv.core.repository.ClaseRepository;
import com.nodo.inv.core.repository.EstructuraRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstructuraService {

    private final EstructuraRepository estructuraRepository;
    private final ClaseRepository claseRepository;

    public List<Estructura> obtenerTodas() {
        return estructuraRepository.findAll();
    }

    @Transactional
    public Estructura guardarEstructura(EstructuraDTO dto) {
        Estructura estructura;
        
        if (dto.getId() != null) {
            estructura = estructuraRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Estructura no encontrada"));
        } else {
            estructura = new Estructura();
        }

        // Buscar y asignar la Clase Padre
        if (dto.getClase() != null && dto.getClase().getId() != null) {
            Clase clasePadre = claseRepository.findById(dto.getClase().getId())
                    .orElseThrow(() -> new RuntimeException("La Clase padre especificada no existe"));
            estructura.setClase(clasePadre);
        }

        estructura.setCodigo(dto.getCodigo());
        estructura.setNombre(dto.getNombre());
        
        return estructuraRepository.save(estructura);
    }

    @Transactional
    public void eliminarEstructura(Long id) {
        try {
            estructuraRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar la Estructura porque tiene Unidades asociadas.");
        }
    }
}