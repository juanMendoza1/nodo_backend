package com.nodo.inv.service;

import com.nodo.inv.core.entity.DominioOperativo;
import com.nodo.inv.core.repository.DominioOperativoRepository;
import com.nodo.inv.dto.DominioOperativoDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DominioOperativoService {

    private final DominioOperativoRepository repository;

    public List<DominioOperativo> obtenerTodos() {
        return repository.findAll();
    }

    @Transactional
    public DominioOperativo guardar(DominioOperativoDTO dto) {
        DominioOperativo dom;
        if (dto.getId() != null) {
            dom = repository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Dominio no encontrado"));
        } else {
            if (repository.findByCodigo(dto.getCodigo().toUpperCase()).isPresent()) {
                throw new RuntimeException("El código de dominio ya existe");
            }
            dom = new DominioOperativo();
        }

        dom.setCodigo(dto.getCodigo().toUpperCase().trim());
        dom.setNombre(dto.getNombre());
        dom.setPrefijoTablas(dto.getPrefijoTablas());
        dom.setServiceProcessorBean(dto.getServiceProcessorBean());
        dom.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return repository.save(dom);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}