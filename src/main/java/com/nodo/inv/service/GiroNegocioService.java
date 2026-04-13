package com.nodo.inv.service;

import com.nodo.inv.core.entity.GiroNegocio;
import com.nodo.inv.core.repository.GiroNegocioRepository;
import com.nodo.inv.dto.GiroNegocioDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GiroNegocioService {

    private final GiroNegocioRepository giroNegocioRepository;

    public List<GiroNegocio> obtenerTodos() {
        return giroNegocioRepository.findAll();
    }

    @Transactional
    public GiroNegocio guardar(GiroNegocioDTO dto) {
        GiroNegocio gn;
        if (dto.getId() != null) {
            gn = giroNegocioRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Giro de negocio no encontrado"));
        } else {
            gn = new GiroNegocio();
        }

        gn.setCodigo(dto.getCodigo());
        gn.setNombre(dto.getNombre());
        gn.setDescripcion(dto.getDescripcion());
        gn.setTemplateMovil(dto.getTemplateMovil());

        return giroNegocioRepository.save(gn);
    }

    @Transactional
    public void eliminar(Long id) {
        try {
            giroNegocioRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar porque hay comercios usando este Giro de Negocio.");
        }
    }
}