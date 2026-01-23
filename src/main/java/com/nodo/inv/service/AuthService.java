package com.nodo.inv.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.inv.entity.Usuario;
import com.nodo.inv.dto.*;
import com.nodo.inv.entity.EmpresaPrograma;
import com.nodo.inv.repository.UsuarioRepository;
import com.nodo.inv.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

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

        // 2. Buscar información extendida del usuario (Empresa y Programas)
        Usuario usuario = usuarioRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error inesperado: Usuario no encontrado post-auth"));

        // 3. Generar Token JWT
        String token = jwtUtil.generateToken(userDetails);

        // 4. Clasificar Roles y Permisos
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList());

        List<String> permisos = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toList());

        // 5. Obtener Programas activos de la empresa del usuario
        List<String> programasActivos = usuario.getEmpresa().getProgramasContratados().stream()
                .filter(ep -> ep.getEstado()) // Solo los que están activos (true)
                .map(ep -> ep.getPrograma().getCodigo())
                .collect(Collectors.toList());

        // 6. Construir respuesta final
        return LoginResponseDTO.builder()
                .token(token)
                .username(userDetails.getUsername())
                .empresaId(usuario.getEmpresa().getId())
                .nombreEmpresa(usuario.getEmpresa().getNombreComercial())
                .roles(roles)
                .permisos(permisos)
                .programas(programasActivos)
                .build();
    }
}