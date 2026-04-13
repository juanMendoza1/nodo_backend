package com.nodo.inv.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.core.entity.Rol;
import com.nodo.inv.core.entity.RolPermiso;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.entity.UsuarioRol;
import com.nodo.inv.core.repository.RolPermisoRepository;
import com.nodo.inv.core.repository.UsuarioRepository;
import com.nodo.inv.core.repository.UsuarioRolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new DisabledException("Usuario no activo");
        }

        // 2. Cargar Roles y Permisos (Usando el método con FETCH que ya tienes)
        List<UsuarioRol> rolesYPermisos = usuarioRolRepository.findByUsuarioWithRolAndPermisos(usuario);

        List<GrantedAuthority> authorities = new ArrayList<>();

        rolesYPermisos.forEach(ur -> {
            // 🔐 IMPORTANTE: Agregar el Rol con prefijo ROLE_
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ur.getRol().getNombre()));
            
            // 🔑 Agregar los Permisos si el Rol tiene la lista cargada
            if (ur.getRol().getRolPermisos() != null) {
                ur.getRol().getRolPermisos().forEach(rp -> {
                    authorities.add(new SimpleGrantedAuthority(rp.getPermiso().getCodigo()));
                });
            }
        });

        // 3. Retornar el usuario de Spring con sus autoridades cargadas
        return new org.springframework.security.core.userdetails.User(
                usuario.getLogin(),
                usuario.getPassword(),
                authorities // 👈 Si esto va vacío, el token sale sin roles
        );
    }
}
