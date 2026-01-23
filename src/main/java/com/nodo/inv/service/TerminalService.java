package com.nodo.inv.service;

import com.nodo.inv.entity.SuscripcionPrograma;
import com.nodo.inv.entity.TerminalDispositivo;
import com.nodo.inv.repository.SuscripcionProgramaRepository;
import com.nodo.inv.repository.TerminalDispositivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TerminalService {

    private final TerminalDispositivoRepository terminalRepository;
    private final SuscripcionProgramaRepository suscripcionRepository;

    @Transactional
    public TerminalDispositivo activarTerminal(Long empresaId, String programaCod, TerminalDispositivo datosTablet) {
        
        // 1. Verificar si la empresa tiene una suscripción activa para ese programa (ej: INV)
        SuscripcionPrograma suscripcion = suscripcionRepository.findByEmpresaIdAndProgramaCodigo(empresaId, programaCod)
                .orElseThrow(() -> new RuntimeException("No existe una suscripción activa para este programa"));

        if (!suscripcion.getActivo()) {
            throw new RuntimeException("La suscripción se encuentra suspendida");
        }

        // 2. Verificar si el dispositivo ya está registrado (Re-vinculación)
        return terminalRepository.findByUuidHardware(datosTablet.getUuidHardware())
            .map(t -> {
                t.setUltimoAcceso(LocalDateTime.now());
                return terminalRepository.save(t);
            })
            .orElseGet(() -> {
                // 3. Si es nuevo, validar cupos disponibles
                if (suscripcion.getDispositivosActivos() >= suscripcion.getMaxDispositivos()) {
                    throw new RuntimeException("Ha alcanzado el límite máximo de dispositivos permitidos (" 
                                               + suscripcion.getMaxDispositivos() + ")");
                }

                // 4. Registrar la nueva terminal
                datosTablet.setSuscripcion(suscripcion);
                datosTablet.setFechaRegistro(LocalDateTime.now());
                datosTablet.setUltimoAcceso(LocalDateTime.now());
                datosTablet.setBloqueado(false);
                
                // Actualizar contador de la suscripción
                suscripcion.setDispositivosActivos(suscripcion.getDispositivosActivos() + 1);
                suscripcionRepository.save(suscripcion);

                return terminalRepository.save(datosTablet);
            });
    }

    @Transactional(readOnly = true)
    public void validarAccesoTerminal(String uuid) {
        TerminalDispositivo terminal = terminalRepository.findByUuidHardware(uuid)
                .orElseThrow(() -> new RuntimeException("Dispositivo no autorizado"));
        
        if (terminal.getBloqueado()) {
            throw new RuntimeException("Este dispositivo ha sido bloqueado por el administrador");
        }
        
        if (!terminal.getSuscripcion().getActivo()) {
            throw new RuntimeException("El servicio de la empresa se encuentra inactivo");
        }
    }
}