package com.nodo.inv.service;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.EmpresaTercero;
import com.nodo.inv.core.entity.Tercero;
import com.nodo.inv.core.entity.Unidad;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.EmpresaTerceroRepository;
import com.nodo.inv.core.repository.TerceroRepository;
import com.nodo.inv.core.repository.UnidadRepository;
import com.nodo.inv.dto.TerceroDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TerceroService {

    private final TerceroRepository terceroRepository;
    private final EmpresaTerceroRepository empresaTerceroRepository;
    private final EmpresaRepository empresaRepository;
    
    // 🔥 IMPORTANTE: Inyectamos el repositorio de Unidades para buscar los parámetros
    private final UnidadRepository unidadRepository;

    @Transactional
    public Tercero crearTercero(TerceroDTO dto, Long empresaId, Usuario usuarioActuo, boolean esGlobal) {
        // 1. Validar si ya existe el documento
        if (terceroRepository.existsByDocumento(dto.getDocumento())) {
            throw new RuntimeException("Ya existe un tercero con el documento: " + dto.getDocumento());
        }

        Tercero tercero = new Tercero();
        
        // 2. Mapear datos básicos y paramétricos
        mapearDtoAEntidad(dto, tercero);

        // 3. Guardar la identidad del Tercero
        Tercero terceroGuardado = terceroRepository.save(tercero);

        // 4. Crear el vínculo en la tabla intermedia EmpresaTercero
        EmpresaTercero vinculo = new EmpresaTercero();
        vinculo.setTercero(terceroGuardado);
        vinculo.setEsGlobal(esGlobal);
        vinculo.setFechaVinculo(LocalDateTime.now());
        vinculo.setCreadoPor(usuarioActuo);

        // Si no es global, se amarra a la empresa que lo crea
        if (!esGlobal) {
            Empresa emp = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
            vinculo.setEmpresa(emp);
        }

        empresaTerceroRepository.save(vinculo);

        return terceroGuardado;
    }

    @Transactional
    public Tercero actualizarTercero(Long id, TerceroDTO dto) {
        Tercero terceroExistente = terceroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tercero no encontrado"));

        // Validar cambio de documento
        if (!terceroExistente.getDocumento().equals(dto.getDocumento())) {
            if (terceroRepository.existsByDocumento(dto.getDocumento())) {
                throw new RuntimeException("Ya existe otro tercero con el documento: " + dto.getDocumento());
            }
        }

        // Mapear nuevos datos y parámetros
        mapearDtoAEntidad(dto, terceroExistente);

        return terceroRepository.save(terceroExistente);
    }

    @Transactional
    public void eliminarTercero(Long id) {
        // Primero eliminamos los vínculos en EmpresaTercero
        List<EmpresaTercero> vinculos = empresaTerceroRepository.findAll().stream()
            .filter(v -> v.getTercero().getId().equals(id))
            .collect(Collectors.toList());
        empresaTerceroRepository.deleteAll(vinculos);

        // Luego eliminamos el tercero
        terceroRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Tercero> listarTercerosVisibles(Long empresaId) {
        List<EmpresaTercero> vinculos = empresaTerceroRepository.findVisibleByEmpresa(empresaId);
        return vinculos.stream().map(EmpresaTercero::getTercero).collect(Collectors.toList());
    }

    public List<Tercero> listarTodos() {
        return terceroRepository.findAll();
    }

    // ========================================================================
    // 🔥 MÉTODO PRIVADO: Centraliza la lógica de mapeo y búsqueda de Unidades
    // ========================================================================
    private void mapearDtoAEntidad(TerceroDTO dto, Tercero tercero) {
        tercero.setDocumento(dto.getDocumento());
        tercero.setNombre(dto.getNombre());
        tercero.setApellido(dto.getApellido());
        tercero.setNombreCompleto(dto.getNombre() + " " + dto.getApellido());
        tercero.setTelefono(dto.getTelefono());
        tercero.setCorreo(dto.getCorreo());

        // Buscar y setear Tipo de Tercero
        if (dto.getTipoTerceroId() != null) {
            Unidad tipoTercero = unidadRepository.findById(dto.getTipoTerceroId())
                    .orElseThrow(() -> new RuntimeException("El Tipo de Tercero seleccionado no es válido"));
            tercero.setTipoTercero(tipoTercero);
        }

        // Buscar y setear Tipo de Identificación
        if (dto.getTipoIdentificacionId() != null) {
            Unidad tipoId = unidadRepository.findById(dto.getTipoIdentificacionId())
                    .orElseThrow(() -> new RuntimeException("El Tipo de Documento seleccionado no es válido"));
            tercero.setTipoIdentificacion(tipoId);
        }
    }
}