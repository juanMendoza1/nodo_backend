package com.nodo.inv.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.inv.core.dto.LoginRequestDTO;
import com.nodo.inv.core.dto.LoginResponseDTO;
import com.nodo.inv.core.entity.ProgramaPermiso;
import com.nodo.inv.core.entity.SuscripcionPrograma;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.repository.ProgramaPermisoRepository;
import com.nodo.inv.core.repository.SuscripcionProgramaRepository;
import com.nodo.inv.core.repository.UsuarioRepository;

import com.nodo.inv.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final ProgramaPermisoRepository programaPermisoRepository; 
    
    // 🔥 EL CAMBIO ESTÁ AQUÍ: Inyectamos el repositorio correcto de Suscripciones
    private final SuscripcionProgramaRepository suscripcionProgramaRepository;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {

        // 1. Autenticación estándar
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 2. Buscar información extendida del usuario
        Usuario usuario = usuarioRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error inesperado: Usuario no encontrado post-auth"));

        // 3. Generar Token JWT
        String token = jwtUtil.generateToken(userDetails);

        // 4. Clasificar Roles y Permisos básicos del usuario
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList());

        List<String> permisosBase = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toList());

        // 🔥 5. LA MAGIA CORREGIDA: Leemos de la tabla de Suscripciones (Lo que hace el SuperAdmin)
        List<String> modulosSuscritos = new ArrayList<>();
        List<String> codigosProgramas = new ArrayList<>();
        
        List<SuscripcionPrograma> suscripcionesActivas = suscripcionProgramaRepository
                .findByEmpresaIdAndActivoTrue(usuario.getEmpresa().getId());

        for (SuscripcionPrograma sub : suscripcionesActivas) {
            codigosProgramas.add(sub.getPrograma().getCodigo());
            
            // Buscamos los permisos (MOD_INVENTARIO, etc.) asociados a cada programa
            List<ProgramaPermiso> pps = programaPermisoRepository.findByPrograma(sub.getPrograma());
            for (ProgramaPermiso pp : pps) {
                modulosSuscritos.add(pp.getPermiso().getCodigo());
            }
        }

        // Unimos permisos base + módulos contratados (sin duplicados)
        List<String> permisosFinales = new ArrayList<>(permisosBase);
        permisosFinales.addAll(modulosSuscritos);
        permisosFinales = permisosFinales.stream().distinct().collect(Collectors.toList());

        // 6. Construir respuesta final
        return LoginResponseDTO.builder()
                .token(token)
                .username(userDetails.getUsername())
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresa().getId())
                .nombreEmpresa(usuario.getEmpresa().getNombreComercial())
                .roles(roles)
                .permisos(permisosFinales) 
                .programas(codigosProgramas)
                .build();
    }
}