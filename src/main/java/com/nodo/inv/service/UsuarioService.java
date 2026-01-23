package com.nodo.inv.service;

import java.time.LocalDateTime;

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

        // Buscamos las entidades relacionadas
        Tercero tercero = terceroRepository.findById(dto.getTerceroId())
                .orElseThrow(() -> new RuntimeException("Tercero no encontrado"));
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        usuario.setTercero(tercero);
        usuario.setEmpresa(empresa);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 🛡️ Asignar Rol ADMIN por defecto (puedes buscarlo por nombre)
        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no configurado"));

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuarioGuardado);
        ur.setRol(rolAdmin);
        ur.setFechaCreacion(LocalDateTime.now());
        usuarioRolRepository.save(ur);

        return usuarioGuardado;
    }
}