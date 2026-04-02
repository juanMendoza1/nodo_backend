package com.nodo.inv.service;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.dto.UsuarioSlotGuardarDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Rol;
import com.nodo.inv.entity.UsuarioOperativo;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.RolRepository;
import com.nodo.inv.repository.UsuarioOperativoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioOperativoService {

    private final UsuarioOperativoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;

    public List<UsuarioOperativo> listarPorEmpresa(Long empresaId) {
        return repository.findByEmpresaId(empresaId);
    }

    // --- NUEVO: GUARDAR / EDITAR SLOT ---
    @Transactional
    public UsuarioOperativo guardarSlot(Long empresaId, UsuarioSlotGuardarDTO dto) {
        UsuarioOperativo slot;

        if (dto.getId() != null) {
            slot = repository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Slot no encontrado"));
            
            // Si mandan un nuevo PIN, lo actualizamos. Si viene vacío, dejamos el actual.
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                slot.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        } else {
            slot = new UsuarioOperativo();
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            Rol rol = rolRepository.findByNombre("OPERATIVO")
                    .orElseThrow(() -> new RuntimeException("Rol OPERATIVO no configurado"));
            
            slot.setEmpresa(empresa);
            slot.setRol(rol);
            slot.setEstado(EstadoUsuario.ACTIVO);
            slot.setFechaCreacion(LocalDateTime.now());
            
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new RuntimeException("El PIN es obligatorio para nuevos usuarios");
            }
            slot.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        slot.setAlias(dto.getAlias().toUpperCase());
        slot.setLogin(dto.getLogin().toUpperCase());

        return repository.save(slot);
    }

    // --- NUEVO: ACTIVAR / DESACTIVAR SLOT ---
    @Transactional
    public void cambiarEstado(Long id) {
        UsuarioOperativo slot = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot no encontrado"));
        
        if (slot.getEstado() == EstadoUsuario.ACTIVO) {
            slot.setEstado(EstadoUsuario.INACTIVO);
        } else {
            slot.setEstado(EstadoUsuario.ACTIVO);
            slot.setIntentosFallidos(0); // Reseteamos bloqueos por si acaso
            slot.setBloqueado(false);
        }
        repository.save(slot);
    }

    public boolean verificarPin(Long id, String pinIngresado) {
        UsuarioOperativo usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot no encontrado"));
        
        // Comparamos el PIN (texto plano) con el hash guardado en uop_password
        return passwordEncoder.matches(pinIngresado, usuario.getPassword());
    }
    
    @Transactional
    public boolean verificarAccesoSeguro(Long id, Long empresaId, String pinIngresado) {
        UsuarioOperativo usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. ¿Está bloqueado temporalmente?
        if (usuario.isBloqueado() && usuario.getFechaBloqueo() != null) {
            if (usuario.getFechaBloqueo().plusMinutes(5).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Usuario bloqueado por exceso de intentos. Intente en 5 min.");
            } else {
                // Ya pasó el tiempo, desbloqueamos automáticamente
                usuario.setBloqueado(false);
                usuario.setIntentosFallidos(0);
            }
        }

        // 2. ¿La empresa es correcta?
        if (!usuario.getEmpresa().getId().equals(empresaId)) {
            return false;
        }

        // 3. Validar PIN
        boolean esValido = passwordEncoder.matches(pinIngresado, usuario.getPassword());

        if (esValido) {
            usuario.setIntentosFallidos(0); // Resetear contador si tiene éxito
            repository.save(usuario);
            return true;
        } else {
            // Aumentar intentos fallidos
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= 3) {
                usuario.setBloqueado(true);
                usuario.setFechaBloqueo(LocalDateTime.now());
            }
            repository.save(usuario);
            return false;
        }
    }
    
    
}