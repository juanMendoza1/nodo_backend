package com.nodo.inv.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.inv.core.dto.LoginRequestDTO;
import com.nodo.inv.core.dto.LoginResponseDTO;
import com.nodo.inv.core.entity.ProgramaPermiso;
import com.nodo.inv.core.entity.SuscripcionPrograma;
import com.nodo.inv.core.entity.Usuario;
import com.nodo.inv.core.repository.EmpresaRepository;
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
    private final SuscripcionProgramaRepository suscripcionProgramaRepository;
    
    // 🔥 Inyectamos estos dos para la lógica del Holding
    private final EmpresaRepository empresaRepository;
    private final UserDetailsService userDetailsService;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByLogin(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Usuario no encontrado post-auth"));
        String token = jwtUtil.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(a -> a.startsWith("ROLE_")).collect(Collectors.toList());
        List<String> permisosBase = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toList());

        List<String> modulosSuscritos = new ArrayList<>();
        List<String> codigosProgramas = new ArrayList<>();
        List<SuscripcionPrograma> suscripcionesActivas = suscripcionProgramaRepository.findByEmpresaIdAndActivoTrue(usuario.getEmpresa().getId());

        for (SuscripcionPrograma sub : suscripcionesActivas) {
            codigosProgramas.add(sub.getPrograma().getCodigo());
            List<ProgramaPermiso> pps = programaPermisoRepository.findByPrograma(sub.getPrograma());
            for (ProgramaPermiso pp : pps) { modulosSuscritos.add(pp.getPermiso().getCodigo()); }
        }

        List<String> permisosFinales = new ArrayList<>(permisosBase);
        permisosFinales.addAll(modulosSuscritos);
        permisosFinales = permisosFinales.stream().distinct().collect(Collectors.toList());

        return LoginResponseDTO.builder()
                .token(token)
                .username(userDetails.getUsername())
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresa().getId())
                .terceroId(usuario.getTercero().getId()) 
                .nombreEmpresa(usuario.getEmpresa().getNombreComercial())
                .roles(roles)
                .permisos(permisosFinales) 
                .programas(codigosProgramas)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO switchContext(Long empresaId, String username) {
        
        // 1. Cargamos al usuario y la empresa destino
        Usuario usuario = usuarioRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        com.nodo.inv.core.entity.Empresa empresaDestino = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        // 2. Validamos Seguridad (Solo el dueño o un SUPER pueden entrar)
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        boolean isSuper = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER"));

        if (!isSuper && !empresaDestino.getTercero().getId().equals(usuario.getTercero().getId())) {
            throw new RuntimeException("Acceso denegado: Esta sucursal no pertenece a tu Holding.");
        }

        // 3. Generamos un nuevo Token fresco
        String token = jwtUtil.generateToken(userDetails);

        // 4. Mapeamos Roles base
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(a -> a.startsWith("ROLE_")).collect(Collectors.toList());
        List<String> permisosBase = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toList());

        List<String> modulosSuscritos = new ArrayList<>();
        List<String> codigosProgramas = new ArrayList<>();
        
        List<SuscripcionPrograma> suscripcionesActivas = suscripcionProgramaRepository
                .findByEmpresaIdAndActivoTrue(empresaId); // Filtramos por la nueva empresa

        for (SuscripcionPrograma sub : suscripcionesActivas) {
            codigosProgramas.add(sub.getPrograma().getCodigo());
            List<ProgramaPermiso> pps = programaPermisoRepository.findByPrograma(sub.getPrograma());
            for (ProgramaPermiso pp : pps) {
                modulosSuscritos.add(pp.getPermiso().getCodigo());
            }
        }

        List<String> permisosFinales = new ArrayList<>(permisosBase);
        permisosFinales.addAll(modulosSuscritos);
        permisosFinales = permisosFinales.stream().distinct().collect(Collectors.toList());

        // 6. Entregamos la nueva "Llave" (Response)
        return LoginResponseDTO.builder()
                .token(token)
                .username(username)
                .usuarioId(usuario.getId())
                .terceroId(usuario.getTercero().getId())
                .empresaId(empresaDestino.getId()) 
                .nombreEmpresa(empresaDestino.getNombreComercial()) // 
                .roles(roles)
                .permisos(permisosFinales) //
                .programas(codigosProgramas)
                .build();
    }
}