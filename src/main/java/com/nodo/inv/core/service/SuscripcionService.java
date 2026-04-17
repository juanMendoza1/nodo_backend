package com.nodo.inv.core.service;

import com.nodo.inv.core.dto.SuscripcionDTO;
import com.nodo.inv.core.dto.SuscripcionGuardarDTO;
import com.nodo.inv.core.entity.CicloFacturacion;
import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Liquidacion;
import com.nodo.inv.core.entity.Programa;
import com.nodo.inv.core.entity.SuscripcionPrograma;
import com.nodo.inv.core.repository.CicloFacturacionRepository;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.LiquidacionRepository;
import com.nodo.inv.core.repository.ProgramaRepository;
import com.nodo.inv.core.repository.SuscripcionProgramaRepository;

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
    private final CicloFacturacionRepository cicloRepo; 
    
    // 🔥 INYECTAMOS EL REPOSITORIO DE LIQUIDACIONES
    private final LiquidacionRepository liquidacionRepo;

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
            suscripcion = suscripcionRepo.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
            
            if (dto.getMaxDispositivos() < suscripcion.getDispositivosActivos()) {
                throw new RuntimeException("No puede asignar menos cupos de los que ya están en uso (" + suscripcion.getDispositivosActivos() + ")");
            }
        } else {
            Optional<SuscripcionPrograma> existente = suscripcionRepo.findByEmpresaIdAndProgramaCodigo(
                    dto.getEmpresa().getId(), 
                    programaRepo.findById(dto.getPrograma().getId())
                        .orElseThrow(() -> new RuntimeException("Programa no encontrado")).getCodigo()
            );

            if (existente.isPresent()) {
                throw new RuntimeException("Esta empresa ya tiene una licencia asignada para este módulo.");
            }

            suscripcion = new SuscripcionPrograma();
            suscripcion.setDispositivosActivos(0); 
            
            Empresa emp = empresaRepo.findById(dto.getEmpresa().getId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            Programa prog = programaRepo.findById(dto.getPrograma().getId())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado"));
            
            suscripcion.setEmpresa(emp);
            suscripcion.setPrograma(prog);
        }

        // Asignación de Ciclo (Obligatorio)
        if (dto.getCicloFacturacion() != null && dto.getCicloFacturacion().getId() != null) {
            CicloFacturacion ciclo = cicloRepo.findById(dto.getCicloFacturacion().getId())
                    .orElseThrow(() -> new RuntimeException("Ciclo de Facturación no encontrado"));
            suscripcion.setCicloFacturacion(ciclo);
        } else {
            throw new RuntimeException("Es obligatorio asignar un Ciclo de Facturación.");
        }

        // 🔥 ASIGNACIÓN DE MATRIZ DE COBRO (Opcional)
        if (dto.getLiquidacion() != null && dto.getLiquidacion().getId() != null) {
            Liquidacion liq = liquidacionRepo.findById(dto.getLiquidacion().getId())
                    .orElseThrow(() -> new RuntimeException("Esquema de liquidación no encontrado"));
            suscripcion.setLiquidacion(liq);
        } else {
            suscripcion.setLiquidacion(null);
        }

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
            throw new RuntimeException("No se puede eliminar la suscripción porque tiene dispositivos vinculados.");
        }
        suscripcionRepo.delete(sub);
    }

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

        if (entidad.getCicloFacturacion() != null) {
            SuscripcionDTO.CicloMinDTO cicloDTO = new SuscripcionDTO.CicloMinDTO();
            cicloDTO.setId(entidad.getCicloFacturacion().getId());
            cicloDTO.setNombre(entidad.getCicloFacturacion().getNombre());
            dto.setCicloFacturacion(cicloDTO);
        }

        // 🔥 MAPEO DE LA LIQUIDACIÓN
        if (entidad.getLiquidacion() != null) {
            SuscripcionDTO.LiquidacionMinDTO liqDTO = new SuscripcionDTO.LiquidacionMinDTO();
            liqDTO.setId(entidad.getLiquidacion().getId());
            liqDTO.setCodigo(entidad.getLiquidacion().getCodigo());
            liqDTO.setNombre(entidad.getLiquidacion().getNombre());
            dto.setLiquidacion(liqDTO);
        }

        return dto;
    }
}