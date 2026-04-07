// src/main/java/com/nodo/inv/service/UnidadService.java
package com.nodo.inv.service;

import com.nodo.inv.dto.UnidadDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Estructura;
import com.nodo.inv.entity.Unidad;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.EstructuraRepository;
import com.nodo.inv.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadService {

    private final UnidadRepository unidadRepository;
    private final EstructuraRepository estructuraRepository;
    private final EmpresaRepository empresaRepository;

    // 🔥 NUEVO: Método usado por el panel del Comercio (Admin)
    public List<Unidad> obtenerPorEstructuraYEmpresa(String codigo, Long empresaId) {
        return unidadRepository.findByEstructuraCodigoAndEmpresa(codigo, empresaId);
    }

    @Transactional
    public Unidad guardarUnidad(UnidadDTO dto) {
        Unidad unidad;
        
        if (dto.getId() != null) {
            unidad = unidadRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Parámetro no encontrado"));
        } else {
            unidad = new Unidad();
            Estructura est = estructuraRepository.findByCodigo(dto.getEstructuraCodigo())
                    .orElseThrow(() -> new RuntimeException("Estructura no configurada"));
            unidad.setEstructura(est);
        }

        unidad.setCodigo(dto.getCodigo());
        unidad.setNombre(dto.getNombre());
        
        // 🔥 MULTI-TENANT LOGIC
        if (dto.getEmpresaId() != null) {
            Empresa emp = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            unidad.setEmpresa(emp);
            unidad.setEsGlobal(false); // Es privada del comercio
        } else {
            // Creada por SuperAdmin
            unidad.setEsGlobal(dto.getEsGlobal() != null ? dto.getEsGlobal() : true);
        }
        
        return unidadRepository.save(unidad);
    }

    @Transactional
    public void eliminarUnidad(Long id) {
        try {
            unidadRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar este parámetro porque ya está en uso.");
        }
    }
    
    public List<Unidad> obtenerTodas() {
        return unidadRepository.findAll();
    }
}