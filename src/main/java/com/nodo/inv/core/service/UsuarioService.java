package com.nodo.inv.core.service;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.core.dto.UsuarioDataDTO;
import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Rol;
import com.nodo.inv.core.entity.Tercero;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.entity.UsuarioRol;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.RolRepository;
import com.nodo.inv.core.repository.TerceroRepository;
import com.nodo.inv.core.repository.UsuarioRepository;
import com.nodo.inv.core.repository.UsuarioRolRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TerceroRepository terceroRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder; 
    
    // 🔥 NUEVOS REPOSITORIOS INYECTADOS
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario guardarUsuario(UsuarioDataDTO dto) {
        Usuario usuario;

        if (dto.getId() != null) {
            usuario = usuarioRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        } else {
            if (usuarioRepository.findByLogin(dto.getLogin()).isPresent()) {
                throw new RuntimeException("El login ya está en uso");
            }
            usuario = new Usuario();
            usuario.setFechaActivacion(LocalDateTime.now());
            
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new RuntimeException("La contraseña es obligatoria para nuevos usuarios");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario.setLogin(dto.getLogin());
        usuario.setEstado(EstadoUsuario.valueOf(dto.getEstado()));

        if (dto.getTercero() != null && dto.getTercero().getId() != null) {
            Tercero tercero = terceroRepository.findById(dto.getTercero().getId())
                    .orElseThrow(() -> new RuntimeException("El Tercero no existe"));
            usuario.setTercero(tercero);
        }

        if (dto.getEmpresa() != null && dto.getEmpresa().getId() != null) {
            Empresa empresa = empresaRepository.findById(dto.getEmpresa().getId())
                    .orElseThrow(() -> new RuntimeException("La Empresa no existe"));
            usuario.setEmpresa(empresa);
        }

        // 1. Guardamos el usuario primero para que tenga un ID
        usuario = usuarioRepository.save(usuario);

        // 🔥 2. GESTIÓN DEL ROL
        if (dto.getRolId() != null) {
            // Limpiamos los roles viejos por si es una edición
            List<UsuarioRol> rolesActuales = usuarioRolRepository.findByUsuario(usuario);
            usuarioRolRepository.deleteAll(rolesActuales);

            // Asignamos el nuevo rol
            Rol rol = rolRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            UsuarioRol nuevoRol = new UsuarioRol();
            nuevoRol.setUsuario(usuario);
            nuevoRol.setRol(rol);
            nuevoRol.setFechaCreacion(LocalDateTime.now());
            usuarioRolRepository.save(nuevoRol);
        }

        return usuario;
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}