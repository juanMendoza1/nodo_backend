package com.nodo.inv.config;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
/*
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TerceroRepository terceroRepository;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // 1. CREAR O RECUPERAR ROLES
        Rol superRol = checkAndCreateRol("SUPER");
        checkAndCreateRol("ADMIN");
        checkAndCreateRol("OPERATIVO");

        // 2. VERIFICAR USUARIO SUPER
        Optional<Usuario> existingUser = usuarioRepository.findByLogin("superadmin");
        
        if (existingUser.isEmpty()) {
            
            // A. Crear Tercero (La identidad)
            Tercero terceroSuper = new Tercero();
            terceroSuper.setDocumento("000-1");
            terceroSuper.setNombre("Super");
            terceroSuper.setApellido("Admin");
            terceroSuper.setNombreCompleto("Super Administrador");
            terceroSuper.setSexo("M");
            terceroSuper.setTelefono("000000");
            terceroSuper.setCorreo("admin@sistemasnodo.com");
            terceroSuper = terceroRepository.save(terceroSuper);

            // B. Crear Empresa (La plataforma proveedora)
            Empresa miEmpresa = new Empresa();
            miEmpresa.setTercero(terceroSuper);
            miEmpresa.setNombreComercial("SISTEMAS NODO");
            miEmpresa.setActivo(true);
            miEmpresa = empresaRepository.save(miEmpresa);

            // C. Crear el Usuario (La cuenta de acceso)
            Usuario usuario = new Usuario();
            usuario.setLogin("superadmin");
            usuario.setPassword(passwordEncoder.encode("admin123"));
            usuario.setEstado(EstadoUsuario.ACTIVO);
            usuario.setTercero(terceroSuper);
            usuario.setEmpresa(miEmpresa);
            usuario.setFechaActivacion(LocalDateTime.now());
            usuario = usuarioRepository.save(usuario);

            // D. ASIGNAR EL ROL SUPER (Puente en la tabla intermedia)
            UsuarioRol ur = new UsuarioRol();
            ur.setUsuario(usuario);
            ur.setRol(superRol);
            ur.setFechaCreacion(LocalDateTime.now());
            usuarioRolRepository.save(ur);

            System.out.println("-----------------------------------------");
            System.out.println("✅ SISTEMA INICIALIZADO");
            System.out.println("👤 USUARIO: superadmin");
            System.out.println("🔑 CLAVE: admin123");
            System.out.println("-----------------------------------------");
        }
    }


    private Rol checkAndCreateRol(String nombreRol) {
        Optional<Rol> rolOpt = rolRepository.findByNombre(nombreRol);
        if (rolOpt.isPresent()) {
            return rolOpt.get();
        } else {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre(nombreRol);
            return rolRepository.save(nuevoRol);
        }
    }
}*/