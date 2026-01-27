package com.nodo.inv.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.dto.UsuarioRegistroDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Rol;
import com.nodo.inv.entity.Tercero;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.entity.UsuarioRol;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.RolRepository;
import com.nodo.inv.repository.TerceroRepository;
import com.nodo.inv.repository.UsuarioRepository;
import com.nodo.inv.repository.UsuarioRolRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TerceroRepository terceroRepository;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario crearUsuarioAdmin(UsuarioRegistroDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setLogin(dto.getLogin());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword())); // 🔐 Encriptación
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaActivacion(LocalDateTime.now());

        Tercero tercero = terceroRepository.findById(dto.getTerceroId())
                .orElseThrow(() -> new RuntimeException("Tercero no encontrado"));
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        usuario.setTercero(tercero);
        usuario.setEmpresa(empresa);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no configurado"));

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuarioGuardado);
        ur.setRol(rolAdmin);
        ur.setFechaCreacion(LocalDateTime.now());
        usuarioRolRepository.save(ur);

        return usuarioGuardado;
    }

    /**
     * NUEVO: Busca todos los usuarios activos de una empresa específica.
     * Estos serán los "slots" que se mostrarán en la tablet.
     */
    public List<Usuario> buscarPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaIdAndEstado(empresaId, EstadoUsuario.ACTIVO);
    }

    /**
     * NUEVO: Verifica si el PIN ingresado en la tablet es correcto.
     * Nota: En este flujo, el 'password' del usuario actúa como su PIN.
     */
    public boolean verificarPin(Long usuarioId, String pinIngresado) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // El passwordEncoder compara el texto plano (pin) con el hash de la DB
        return passwordEncoder.matches(pinIngresado, usuario.getPassword());
    }
}