package com.nodo.inv.service;

import com.nodo.inv.dto.ClaseDTO;
import com.nodo.inv.entity.Clase;
import com.nodo.inv.repository.ClaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaseService {

    private final ClaseRepository claseRepository;

    public List<Clase> obtenerTodas() {
        return claseRepository.findAll();
    }

    @Transactional
    public Clase guardarClase(ClaseDTO dto) {
        Clase clase;
        
        if (dto.getId() != null) {
            clase = claseRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Clase no encontrada"));
        } else {
            clase = new Clase();
        }

        clase.setCodigo(dto.getCodigo());
        clase.setNombre(dto.getNombre());
        clase.setDescripcion(dto.getDescripcion());
        
        return claseRepository.save(clase);
    }

    @Transactional
    public void eliminarClase(Long id) {
        try {
            claseRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar la Clase porque tiene Estructuras asociadas.");
        }
    }
}