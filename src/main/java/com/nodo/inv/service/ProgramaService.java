package com.nodo.inv.service;

import com.nodo.inv.dto.ProgramaDTO;
import com.nodo.inv.entity.Programa;
import com.nodo.inv.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramaService {

    private final ProgramaRepository programaRepository;

    // Listar todos los programas para las tarjetas
    public List<Programa> obtenerTodos() {
        return programaRepository.findAll();
    }

    // Crear o Actualizar
    @Transactional
    public Programa guardarPrograma(ProgramaDTO dto) {
        Programa programa;
        
        if (dto.getId() != null) {
            // Edición
            programa = programaRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado con ID: " + dto.getId()));
        } else {
            // Creación: Validar que el código no exista
            if (programaRepository.existsByCodigo(dto.getCodigo())) {
                throw new RuntimeException("Ya existe un Programa con el código: " + dto.getCodigo());
            }
            programa = new Programa();
        }

        // Mapeo de datos
        programa.setCodigo(dto.getCodigo().toUpperCase()); // Forzar a mayúsculas por estándar
        programa.setNombre(dto.getNombre());
        programa.setDescripcion(dto.getDescripcion());
        programa.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        programa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return programaRepository.save(programa);
    }

    // Eliminar
    @Transactional
    public void eliminarPrograma(Long id) {
        try {
            programaRepository.deleteById(id);
        } catch (Exception e) {
            // Si PostgreSQL lanza error de llave foránea (Ej. porque ya hay Suscripciones atadas a este programa)
            throw new RuntimeException("No se puede eliminar el Programa porque ya está en uso (Suscripciones activas).");
        }
    }
}