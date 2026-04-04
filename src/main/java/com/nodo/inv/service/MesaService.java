package com.nodo.inv.service;

import com.nodo.inv.dto.MesaDTO;
import com.nodo.inv.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;

    @Transactional(readOnly = true)
    public List<MesaDTO> obtenerEstadoMesasPorEmpresa(Long empresaId) {
        // Buscamos todas las mesas de la empresa y las mapeamos a DTO
        return mesaRepository.findByEmpresaId(empresaId)
                .stream()
                .map(MesaDTO::new)
                .collect(Collectors.toList());
    }
}