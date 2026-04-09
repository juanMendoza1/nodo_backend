package com.nodo.inv.service;

import com.nodo.inv.dto.UnidadDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Estructura;
import com.nodo.inv.entity.Unidad;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.EstructuraRepository;
import com.nodo.inv.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadService {

    private final UnidadRepository unidadRepository;
    private final EstructuraRepository estructuraRepository;
    private final EmpresaRepository empresaRepository;

    @Cacheable(value = "unidadesPorEstructura", key = "#codigo + '-' + #empresaId")
    public List<Unidad> obtenerPorEstructuraYEmpresa(String codigo, Long empresaId) {
        return unidadRepository.findByEstructuraCodigoAndEmpresa(codigo, empresaId);
    }

    @Cacheable(value = "unidadesTodas")
    public List<Unidad> obtenerTodas() {
        return unidadRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = {"unidadesPorEstructura", "unidadesTodas"}, allEntries = true)
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
        
        if (dto.getEmpresaId() != null) {
            Empresa emp = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            unidad.setEmpresa(emp);
            unidad.setEsGlobal(false); 
        } else {
            unidad.setEsGlobal(dto.getEsGlobal() != null ? dto.getEsGlobal() : true);
        }
        
        return unidadRepository.save(unidad);
    }

    @Transactional
    @CacheEvict(value = {"unidadesPorEstructura", "unidadesTodas"}, allEntries = true)
    public void eliminarUnidad(Long id) {
        try {
            unidadRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar este parámetro porque ya está en uso.");
        }
    }
}