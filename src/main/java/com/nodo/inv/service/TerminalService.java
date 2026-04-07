package com.nodo.inv.service;

import com.nodo.inv.dto.TerminalCupoDTO;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TerminalService {

    private final TerminalDispositivoRepository terminalRepository;
    private final SuscripcionProgramaRepository suscripcionRepository;
    private final TerminalTokenRepository tokenRepository;
    private final EmpresaRepository empresaRepository;
    private final ProgramaRepository programaRepository;

    // --- MÉTODOS ORIGINALES ---

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
                datosTablet.setEmpresa(suscripcion.getEmpresa());
                datosTablet.setPrograma(suscripcion.getPrograma());
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

    // --- NUEVOS MÉTODOS PARA FLUJO QR (PLATAFORMA WEB) ---

    @Transactional(readOnly = true)
    public TerminalCupoDTO consultarCuposDisponibles(Long empresaId, String programaCod) {
        // En lugar de lanzar excepción, usamos Optional
        var subOpt = suscripcionRepository.findByEmpresaIdAndProgramaCodigo(empresaId, programaCod);

        // Si no existe la suscripción o está inactiva, devolvemos 0 cupos
        if (subOpt.isEmpty() || !subOpt.get().getActivo()) {
            return TerminalCupoDTO.builder()
                    .maxDispositivos(0)
                    .dispositivosActivos(0)
                    .disponibles(0)
                    .build();
        }

        SuscripcionPrograma sub = subOpt.get();
        return TerminalCupoDTO.builder()
                .maxDispositivos(sub.getMaxDispositivos())
                .dispositivosActivos(sub.getDispositivosActivos())
                .disponibles(sub.getMaxDispositivos() - sub.getDispositivosActivos())
                .build();
    }

    @Transactional
    public String generarTokenRegistro(Long empresaId, String programaCod) {
        // 1. Validar disponibilidad de cupos antes de generar el token
        TerminalCupoDTO cupos = consultarCuposDisponibles(empresaId, programaCod);
        if (cupos.getDisponibles() <= 0) {
            throw new RuntimeException("No tiene cupos disponibles para registrar nuevas terminales");
        }

        // 2. Obtener entidades relacionadas
        Empresa emp = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Programa prog = programaRepository.findByCodigo(programaCod)
                .orElseThrow(() -> new RuntimeException("Programa no encontrado"));

        // 3. Crear y guardar el token temporal (Expira en 10 minutos)
        TerminalTokenRegistro registro = new TerminalTokenRegistro();
        registro.setToken(UUID.randomUUID().toString());
        registro.setEmpresa(emp);
        registro.setPrograma(prog);
        registro.setFechaCreacion(LocalDateTime.now());
        registro.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));
        registro.setUsado(false);
        
        tokenRepository.save(registro);
        
        return registro.getToken();
    }
    
    @Transactional
    public TerminalDispositivo vincularPorQr(String tokenStr, TerminalDispositivo datosTablet) {
        // 1. Validar el token en la base de datos
        TerminalTokenRegistro registro = tokenRepository.findByTokenAndUsadoFalse(tokenStr)
                .orElseThrow(() -> new RuntimeException("El código QR no es válido o ya fue utilizado"));

        // 2. Verificar expiración
        if (registro.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código QR ha expirado. Por favor genera uno nuevo.");
        }

        // 3. Marcar el token como usado para que no se pueda reutilizar
        registro.setUsado(true);
        tokenRepository.save(registro);

        // 4. Reutilizar la lógica de activación existente
        // Esto validará cupos y registrará la terminal amarrada a la empresa y programa del token
        return activarTerminal(
                registro.getEmpresa().getId(), 
                registro.getPrograma().getCodigo(), 
                datosTablet
        );
    }
    
    public List<TerminalDispositivo> listarPorEmpresa(Long empresaId) {
        return terminalRepository.findBySuscripcionEmpresaId(empresaId);
    }
    
    @Transactional
    public TerminalDispositivo alternarBloqueo(Long idTerminal) {
        TerminalDispositivo terminal = terminalRepository.findById(idTerminal)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrada"));
        
        // Invertimos el estado actual (Si era false pasa a true, y viceversa)
        terminal.setBloqueado(!terminal.getBloqueado());
        
        return terminalRepository.save(terminal);
    }

    @Transactional
    public void desvincularTerminal(Long idTerminal) {
        TerminalDispositivo terminal = terminalRepository.findById(idTerminal)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrada"));

        SuscripcionPrograma suscripcion = terminal.getSuscripcion();
        
        // 1. Restar el cupo usado en la suscripción para liberarlo
        if (suscripcion.getDispositivosActivos() > 0) {
            suscripcion.setDispositivosActivos(suscripcion.getDispositivosActivos() - 1);
            suscripcionRepository.save(suscripcion);
        }

        // 2. Eliminar el dispositivo de la base de datos
        // (Opcionalmente, en lugar de eliminar, podrías cambiarle un estado a "ELIMINADO" si quieres guardar el historial)
        terminalRepository.delete(terminal);
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> consultarCuposPorEmpresa(Long empresaId) {
        List<SuscripcionPrograma> suscripciones = suscripcionRepository.findByEmpresaIdAndActivoTrue(empresaId);

        return suscripciones.stream().map(sub -> Map.<String, Object>of(
                "programaCod", sub.getPrograma().getCodigo(),
                "programaNombre", sub.getPrograma().getNombre(),
                "maxDispositivos", sub.getMaxDispositivos(),
                "dispositivosActivos", sub.getDispositivosActivos(),
                "disponibles", sub.getMaxDispositivos() - sub.getDispositivosActivos()
        )).toList();
    }   
    
}