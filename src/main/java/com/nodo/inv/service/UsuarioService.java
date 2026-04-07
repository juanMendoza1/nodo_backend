package com.nodo.inv.service;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.dto.UsuarioDataDTO;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Tercero;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.TerceroRepository;
import com.nodo.inv.repository.UsuarioRepository;
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
    // 🔥 IMPORTANTE: Inyectamos el encriptador
    private final PasswordEncoder passwordEncoder; 

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario guardarUsuario(UsuarioDataDTO dto) {
        Usuario usuario;

        if (dto.getId() != null) {
            usuario = usuarioRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // 🔥 TRATO ESPECIAL DE LA CONTRASEÑA EN EDICIÓN
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                // Si mandaron contraseña, la encriptamos y la seteamos
                usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            // Si vino vacía, simplemente no hacemos nada y conserva su hash actual en la BD.
            
        } else {
            // Creación
            if (usuarioRepository.findByLogin(dto.getLogin()).isPresent()) {
                throw new RuntimeException("El login ya está en uso");
            }
            usuario = new Usuario();
            usuario.setFechaActivacion(LocalDateTime.now());
            
            // 🔥 TRATO ESPECIAL EN CREACIÓN: Es obligatoria
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new RuntimeException("La contraseña es obligatoria para nuevos usuarios");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario.setLogin(dto.getLogin());
        usuario.setEstado(EstadoUsuario.valueOf(dto.getEstado()));

        // Asignar Tercero (Persona real)
        if (dto.getTercero() != null && dto.getTercero().getId() != null) {
            Tercero tercero = terceroRepository.findById(dto.getTercero().getId())
                    .orElseThrow(() -> new RuntimeException("El Tercero no existe"));
            usuario.setTercero(tercero);
        }

        // Asignar Empresa (Tenant)
        if (dto.getEmpresa() != null && dto.getEmpresa().getId() != null) {
            Empresa empresa = empresaRepository.findById(dto.getEmpresa().getId())
                    .orElseThrow(() -> new RuntimeException("La Empresa no existe"));
            usuario.setEmpresa(empresa);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}