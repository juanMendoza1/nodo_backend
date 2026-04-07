package com.nodo.inv.service;

import com.nodo.inv.dto.ProgramaDTO;
import com.nodo.inv.entity.Permiso;
import com.nodo.inv.entity.Programa;
import com.nodo.inv.entity.ProgramaPermiso;
import com.nodo.inv.repository.PermisoRepository;
import com.nodo.inv.repository.ProgramaPermisoRepository;
import com.nodo.inv.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramaService {

    private final ProgramaRepository programaRepository;
    private final PermisoRepository permisoRepository;
    private final ProgramaPermisoRepository programaPermisoRepository;

    @Transactional(readOnly = true)
    public List<ProgramaDTO> obtenerTodos() {
        return programaRepository.findAll().stream().map(this::mapearADto).collect(Collectors.toList());
    }

    @Transactional
    public ProgramaDTO guardarPrograma(ProgramaDTO dto) {
        Programa programa;
        
        if (dto.getId() != null) {
            programa = programaRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado"));
            // Limpiamos las fichas de lego anteriores
            programaPermisoRepository.deleteByPrograma(programa);
        } else {
            if (programaRepository.existsByCodigo(dto.getCodigo())) {
                throw new RuntimeException("Ya existe un Programa con el código: " + dto.getCodigo());
            }
            programa = new Programa();
        }

        programa.setCodigo(dto.getCodigo().toUpperCase());
        programa.setNombre(dto.getNombre());
        programa.setDescripcion(dto.getDescripcion());
        programa.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        programa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Programa guardado = programaRepository.save(programa);

        // 🔥 GESTIÓN DE LAS PIEZAS DE LEGO (PERMISOS)
        if (dto.getPermisosIds() != null && !dto.getPermisosIds().isEmpty()) {
            for (Long permisoId : dto.getPermisosIds()) {
                Permiso permiso = permisoRepository.findById(permisoId)
                        .orElseThrow(() -> new RuntimeException("Permiso no válido"));
                ProgramaPermiso pp = new ProgramaPermiso();
                pp.setPrograma(guardado);
                pp.setPermiso(permiso);
                programaPermisoRepository.save(pp);
            }
        }

        return mapearADto(guardado);
    }

    @Transactional
    public void eliminarPrograma(Long id) {
        Programa p = programaRepository.findById(id).orElseThrow(() -> new RuntimeException("Programa no encontrado"));
        programaPermisoRepository.deleteByPrograma(p);
        programaRepository.delete(p);
    }

    // MAPPER
    private ProgramaDTO mapearADto(Programa p) {
        ProgramaDTO dto = new ProgramaDTO();
        dto.setId(p.getId());
        dto.setCodigo(p.getCodigo());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setVersion(p.getVersion());
        dto.setActivo(p.getActivo());

        // Extraemos las fichas de lego para mandarlas a React
        List<ProgramaPermiso> permisos = programaPermisoRepository.findByPrograma(p);
        dto.setPermisosIds(permisos.stream().map(pp -> pp.getPermiso().getId()).collect(Collectors.toList()));
        dto.setPermisosCodigos(permisos.stream().map(pp -> pp.getPermiso().getCodigo()).collect(Collectors.toList()));
        
        return dto;
    }
}