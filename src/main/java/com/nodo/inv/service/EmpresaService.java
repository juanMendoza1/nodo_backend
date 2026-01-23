package com.nodo.inv.service;

import org.springframework.stereotype.Service;

import com.nodo.inv.dto.EmpresaRegistroDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Tercero;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.TerceroRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final TerceroRepository terceroRepository;

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
}