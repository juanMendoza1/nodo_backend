package com.nodo.inv.service;

import com.nodo.inv.entity.Unidad;
import com.nodo.inv.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadService {

    private final UnidadRepository unidadRepository;

    public List<Unidad> obtenerPorEstructura(String codigo) {
        return unidadRepository.findByEstructuraCodigo(codigo);
    }

    public List<Unidad> listarTodas() {
        return unidadRepository.findAll();
    }
}