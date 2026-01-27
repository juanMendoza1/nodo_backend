package com.nodo.inv.service;

import com.nodo.inv.entity.UsuarioOperativo;
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

    public List<UsuarioOperativo> listarPorEmpresa(Long empresaId) {
        // Retornamos solo los que pertenecen a la empresa
        return repository.findByEmpresaId(empresaId);
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