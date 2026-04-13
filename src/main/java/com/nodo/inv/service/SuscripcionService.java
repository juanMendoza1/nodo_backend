package com.nodo.inv.service;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Programa;
import com.nodo.inv.core.entity.SuscripcionPrograma;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.ProgramaRepository;
import com.nodo.inv.core.repository.SuscripcionProgramaRepository;
import com.nodo.inv.dto.SuscripcionDTO;
import com.nodo.inv.dto.SuscripcionGuardarDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final SuscripcionProgramaRepository suscripcionRepo;
    private final EmpresaRepository empresaRepo;
    private final ProgramaRepository programaRepo;

    @Transactional(readOnly = true)
    public List<SuscripcionDTO> obtenerTodas() {
        return suscripcionRepo.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SuscripcionDTO guardarSuscripcion(SuscripcionGuardarDTO dto) {
        SuscripcionPrograma suscripcion;

        if (dto.getId() != null) {
            // 🔥 EDICIÓN: Buscamos la existente
            suscripcion = suscripcionRepo.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
            
            // Validar que no le bajen los cupos por debajo de lo que ya está en uso
            if (dto.getMaxDispositivos() < suscripcion.getDispositivosActivos()) {
                throw new RuntimeException("No puede asignar menos cupos de los que ya están en uso actualmente (" + suscripcion.getDispositivosActivos() + ")");
            }
        } else {
            // 🔥 CREACIÓN: Validamos duplicados
            Optional<SuscripcionPrograma> existente = suscripcionRepo.findByEmpresaIdAndProgramaCodigo(
                    dto.getEmpresa().getId(), 
                    programaRepo.findById(dto.getPrograma().getId()).get().getCodigo()
            );

            if (existente.isPresent()) {
                throw new RuntimeException("Esta empresa ya tiene una licencia asignada para este módulo.");
            }

            suscripcion = new SuscripcionPrograma();
            suscripcion.setDispositivosActivos(0); // Proteger: Siempre nace en 0
            
            Empresa emp = empresaRepo.findById(dto.getEmpresa().getId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            Programa prog = programaRepo.findById(dto.getPrograma().getId())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado"));
            
            suscripcion.setEmpresa(emp);
            suscripcion.setPrograma(prog);
        }

        // Actualización de campos permitidos
        suscripcion.setMaxDispositivos(dto.getMaxDispositivos());
        suscripcion.setFechaVencimiento(dto.getFechaVencimiento());
        suscripcion.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        suscripcion = suscripcionRepo.save(suscripcion);
        return mapearADTO(suscripcion);
    }

    @Transactional
    public void eliminarSuscripcion(Long id) {
        SuscripcionPrograma sub = suscripcionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
        
        if (sub.getDispositivosActivos() > 0) {
            throw new RuntimeException("No se puede eliminar la suscripción porque tiene " + sub.getDispositivosActivos() + " dispositivos vinculados actualmente.");
        }
        
        suscripcionRepo.delete(sub);
    }

    // --------------------------------------------------------
    // MAPPER: Convierte la Entidad Pesada a un DTO Ligero
    // --------------------------------------------------------
    private SuscripcionDTO mapearADTO(SuscripcionPrograma entidad) {
        SuscripcionDTO dto = new SuscripcionDTO();
        dto.setId(entidad.getId());
        dto.setMaxDispositivos(entidad.getMaxDispositivos());
        dto.setDispositivosActivos(entidad.getDispositivosActivos());
        dto.setFechaVencimiento(entidad.getFechaVencimiento());
        dto.setActivo(entidad.getActivo());

        if (entidad.getEmpresa() != null) {
            SuscripcionDTO.EmpresaMinDTO empDTO = new SuscripcionDTO.EmpresaMinDTO();
            empDTO.setId(entidad.getEmpresa().getId());
            empDTO.setNombreComercial(entidad.getEmpresa().getNombreComercial());
            dto.setEmpresa(empDTO);
        }

        if (entidad.getPrograma() != null) {
            SuscripcionDTO.ProgramaMinDTO progDTO = new SuscripcionDTO.ProgramaMinDTO();
            progDTO.setId(entidad.getPrograma().getId());
            progDTO.setCodigo(entidad.getPrograma().getCodigo());
            progDTO.setNombre(entidad.getPrograma().getNombre());
            dto.setPrograma(progDTO);
        }

        return dto;
    }
}