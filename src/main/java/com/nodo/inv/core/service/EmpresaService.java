package com.nodo.inv.core.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nodo.inv.core.dto.EmpresaRegistroDTO;
import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Tercero;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.GiroNegocioRepository;
import com.nodo.inv.core.repository.TerceroRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final TerceroRepository terceroRepository;
    private final GiroNegocioRepository giroNegocioRepository;

    @Transactional
    public Empresa crearEmpresa(EmpresaRegistroDTO dto) {
        // 1. Crear el Tercero primero
        Tercero tercero = new Tercero();
        tercero.setDocumento(dto.getDocumento());
        tercero.setNombre(dto.getNombre());
        tercero.setApellido(dto.getApellido());
        tercero.setNombreCompleto(dto.getNombre() + " " + dto.getApellido());
        tercero.setCorreo(dto.getCorreo());
        tercero = terceroRepository.save(tercero);

        // 2. Crear la Empresa vinculada al Tercero
        Empresa empresa = new Empresa();
        empresa.setTercero(tercero);
        empresa.setNombreComercial(dto.getNombreComercial());
        empresa.setActivo(true);
        
        return empresaRepository.save(empresa);
    }
    
    public List<Empresa> obtenerTodas() {
        return empresaRepository.findAll();
    }

    @Transactional
    public Empresa guardarEmpresa(com.nodo.inv.core.dto.EmpresaDataDTO dto) {
        Empresa empresa;
        if (dto.getId() != null) {
            empresa = empresaRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Comercio no encontrado"));
        } else {
            empresa = new Empresa();
        }

        empresa.setNombreComercial(dto.getNombreComercial());
        empresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Asignar el Tercero
        if (dto.getTercero() != null && dto.getTercero().getId() != null) {
            com.nodo.inv.core.entity.Tercero tercero = terceroRepository.findById(dto.getTercero().getId())
                    .orElseThrow(() -> new RuntimeException("El Tercero seleccionado no existe"));
            empresa.setTercero(tercero);
        }

        // Asignar el Giro de Negocio
        if (dto.getGiroNegocio() != null && dto.getGiroNegocio().getId() != null) {
            com.nodo.inv.core.entity.GiroNegocio giro = giroNegocioRepository.findById(dto.getGiroNegocio().getId())
                    .orElseThrow(() -> new RuntimeException("El Giro de Negocio seleccionado no existe"));
            empresa.setGiroNegocio(giro);
        }

        return empresaRepository.save(empresa);
    }

    @Transactional
    public void eliminarEmpresa(Long id) {
        empresaRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Empresa> listarPorPropietario(Long terceroId) {
        return empresaRepository.findByTerceroId(terceroId);
    }
    
}