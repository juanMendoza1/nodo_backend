package com.nodo.inv.service;

import com.nodo.inv.dto.UnidadDTO;
import com.nodo.inv.entity.Estructura;
import com.nodo.inv.entity.Unidad;
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

    public List<Unidad> obtenerPorEstructura(String codigo) {
        return unidadRepository.findByEstructuraCodigo(codigo);
    }

    @Transactional
    public Unidad guardarUnidad(UnidadDTO dto) {
        Unidad unidad;
        
        if (dto.getId() != null) {
            // Editar existente
            unidad = unidadRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Parámetro no encontrado"));
        } else {
            // Crear nuevo
            unidad = new Unidad();
            Estructura est = estructuraRepository.findByCodigo(dto.getEstructuraCodigo())
                    .orElseThrow(() -> new RuntimeException("Estructura no configurada"));
            unidad.setEstructura(est);
        }

        unidad.setCodigo(dto.getCodigo());
        unidad.setNombre(dto.getNombre());
        
        return unidadRepository.save(unidad);
    }

    @Transactional
    public void eliminarUnidad(Long id) {
        try {
            unidadRepository.deleteById(id);
        } catch (Exception e) {
            // Si el parámetro ya se usó en un producto, PostgreSQL bloqueará el borrado por llave foránea.
            throw new RuntimeException("No se puede eliminar este parámetro porque ya está siendo usado por uno o más productos.");
        }
    }
}