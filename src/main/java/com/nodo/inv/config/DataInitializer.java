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

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TerceroRepository terceroRepository;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final ProgramaRepository programaRepository;
    private final EmpresaProgramaRepository empresaProgramaRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final EmpresaTerceroRepository empresaTerceroRepository;
    private final GiroNegocioRepository giroNegocioRepository; // Nueva dependencia
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // 1. CREACIÓN DE GIROS DE NEGOCIO (VERTICALIZACIÓN)
        GiroNegocio giroBillar = checkAndCreateGiro("RESTAURANTE / BILLAR", "REST_BILL", "ARENA_DUELO");
        GiroNegocio giroZapa = checkAndCreateGiro("ZAPATERÍA / RETAIL", "ZAPA", "POS_ESTANDAR");
        GiroNegocio giroSuper = checkAndCreateGiro("SUPERMERCADO", "SUPER_MARKET", "LECTOR_BARRAS");

        // 2. ROLES Y PERMISOS BASE
        Rol superRol = checkAndCreateRol("SUPER");
        Rol adminRol = checkAndCreateRol("ADMIN");
        
        Permiso invView = checkAndCreatePermiso("INV_VIEW", "Ver Inventario");
        Permiso invEdit = checkAndCreatePermiso("INV_EDIT", "Editar Inventario");

        asignarPermisoARol(adminRol, invView);
        asignarPermisoARol(adminRol, invEdit);

        // 3. PROGRAMAS DEL SISTEMA
        Programa progInv = checkAndCreatePrograma("Inventario", "INV");

        // 4. ESTRUCTURA DE JUAN (SUPER ADMIN / DUEÑO PLATAFORMA)
        if (usuarioRepository.findByLogin("superadmin").isEmpty()) {
            Tercero terJuan = crearTerceroBasic("1010", "Juan", "Admin", "juan@nodo.com");
            Tercero terEmpNodo = crearTerceroBasic("9001", "Sistemas", "Nodo", "contacto@nodo.com");
            
            // Juan pertenece a una empresa de tecnología
            Empresa empNodo = crearEmpresaBasic(terEmpNodo, "SISTEMAS NODO", giroZapa); 
            
            Usuario userJuan = crearUsuarioBasic("superadmin", "admin123", terJuan, empNodo, superRol);

            // Ejemplo: Juan crea un Proveedor Global
            Tercero provGlobal = crearTerceroBasic("NIT-POSTOBON", "Postobón", "S.A.", "ventas@postobon.com");
            vincularTerceroAEmpresa(provGlobal, null, userJuan, true);
        }

        // 5. ESTRUCTURA DE DIEGO (CLIENTE / DUEÑO DE BILLAR)
        if (usuarioRepository.findByLogin("diego_admin").isEmpty()) {
            Tercero terDiego = crearTerceroBasic("2020", "Diego", "Cliente", "diego@billares.com");
            Tercero terEmpDiego = crearTerceroBasic("8002", "Billares", "Diego", "ventas@billaresdiego.com");
            
            // Diego usa el Giro de Negocio de Billar para activar la App Móvil correctamente
            Empresa empDiego = crearEmpresaBasic(terEmpDiego, "BILLARES DIEGO", giroBillar);

            vincularPrograma(empDiego, progInv);

            Usuario userDiego = crearUsuarioBasic("diego_admin", "diego123", terDiego, empDiego, adminRol);

            // Ejemplo: Diego crea un Proveedor Privado
            Tercero provPrivado = crearTerceroBasic("PROV-LOCAL", "Distribuidora", "El Vecino", "vecino@mail.com");
            vincularTerceroAEmpresa(provPrivado, empDiego, userDiego, false);
            
            System.out.println("-----------------------------------------");
            System.out.println("🚀 ESCENARIO SaaS VERTICALIZADO LISTO");
            System.out.println("🏢 GIRO: REST_BILL (Habilitado para App Móvil)");
            System.out.println("👤 JUAN (SUPER): Empresa Nodo Creada.");
            System.out.println("👤 DIEGO (ADMIN): Billares Diego con Giro de Negocio asignado.");
            System.out.println("-----------------------------------------");
        }
    }

    // --- MÉTODOS AUXILIARES DE CREACIÓN ---

    private GiroNegocio checkAndCreateGiro(String nom, String cod, String template) {
        return giroNegocioRepository.findByCodigo(cod).orElseGet(() -> {
            GiroNegocio gn = new GiroNegocio();
            gn.setNombre(nom);
            gn.setCodigo(cod);
            gn.setTemplateMovil(template);
            return giroNegocioRepository.save(gn);
        });
    }

    private void vincularTerceroAEmpresa(Tercero t, Empresa e, Usuario u, boolean global) {
        EmpresaTercero et = new EmpresaTercero();
        et.setTercero(t);
        et.setEmpresa(e);
        et.setCreadoPor(u);
        et.setEsGlobal(global);
        et.setFechaVinculo(LocalDateTime.now());
        empresaTerceroRepository.save(et);
    }

    private Tercero crearTerceroBasic(String doc, String nom, String ape, String mail) {
        Tercero t = new Tercero();
        t.setDocumento(doc);
        t.setNombre(nom);
        t.setApellido(ape);
        t.setNombreCompleto(nom + " " + ape);
        t.setCorreo(mail);
        return terceroRepository.save(t);
    }

    private Empresa crearEmpresaBasic(Tercero t, String nombre, GiroNegocio gn) {
        Empresa e = new Empresa();
        e.setTercero(t);
        e.setNombreComercial(nombre);
        e.setGiroNegocio(gn); // Asignación del Giro
        e.setActivo(true);
        return empresaRepository.save(e);
    }

    private Usuario crearUsuarioBasic(String login, String pass, Tercero t, Empresa e, Rol r) {
        Usuario u = new Usuario();
        u.setLogin(login);
        u.setPassword(passwordEncoder.encode(pass));
        u.setEstado(EstadoUsuario.ACTIVO);
        u.setTercero(t);
        u.setEmpresa(e);
        u.setFechaActivacion(LocalDateTime.now());
        u = usuarioRepository.save(u);

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(u);
        ur.setRol(r);
        ur.setFechaCreacion(LocalDateTime.now());
        usuarioRolRepository.save(ur);
        return u;
    }

    private Rol checkAndCreateRol(String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            return rolRepository.save(r);
        });
    }

    private Permiso checkAndCreatePermiso(String cod, String desc) {
        return permisoRepository.findByCodigo(cod).orElseGet(() -> {
            Permiso p = new Permiso();
            p.setCodigo(cod);
            p.setDescripcion(desc);
            return permisoRepository.save(p);
        });
    }

    private void asignarPermisoARol(Rol r, Permiso p) {
        if (rolPermisoRepository.findByRol(r).stream().noneMatch(rp -> rp.getPermiso().getCodigo().equals(p.getCodigo()))) {
            RolPermiso rp = new RolPermiso();
            rp.setRol(r);
            rp.setPermiso(p);
            rolPermisoRepository.save(rp);
        }
    }

    private Programa checkAndCreatePrograma(String nom, String cod) {
        return programaRepository.findByCodigo(cod).orElseGet(() -> {
            Programa p = new Programa();
            p.setNombre(nom);
            p.setCodigo(cod);
            p.setActivo(true);
            return programaRepository.save(p);
        });
    }

    private void vincularPrograma(Empresa e, Programa p) {
        EmpresaPrograma ep = new EmpresaPrograma();
        ep.setEmpresa(e);
        ep.setPrograma(p);
        ep.setEstado(true);
        ep.setFechaActivacion(LocalDateTime.now());
        empresaProgramaRepository.save(ep);
    }
}