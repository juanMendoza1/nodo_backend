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
    private final GiroNegocioRepository giroNegocioRepository;
    private final SuscripcionProgramaRepository suscripcionProgramaRepository;
    private final TerminalDispositivoRepository terminalDispositivoRepository;
    private final UsuarioOperativoRepository usuarioOperativoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // 1. CREACIÓN DE GIROS DE NEGOCIO
        GiroNegocio giroBillar = checkAndCreateGiro("RESTAURANTE / BILLAR", "REST_BILL", "ARENA_DUELO");
        GiroNegocio giroZapa = checkAndCreateGiro("ZAPATERÍA / RETAIL", "ZAPA", "POS_ESTANDAR");
        GiroNegocio giroSuper = checkAndCreateGiro("SUPERMERCADO", "SUPER_MARKET", "LECTOR_BARRAS");

        // 2. ROLES Y PERMISOS BASE
        Rol superRol = checkAndCreateRol("SUPER");
        Rol adminRol = checkAndCreateRol("ADMIN");
        Rol opRol = checkAndCreateRol("OPERATIVO");
        
        Permiso invView = checkAndCreatePermiso("INV_VIEW", "Ver Inventario");
        Permiso invEdit = checkAndCreatePermiso("INV_EDIT", "Editar Inventario");

        asignarPermisoARol(adminRol, invView);
        asignarPermisoARol(adminRol, invEdit);
        asignarPermisoARol(opRol, invView);

        // 3. PROGRAMAS DEL SISTEMA
        Programa progInv = checkAndCreatePrograma("Inventario", "INV");

        // 4. ESTRUCTURA DE JUAN (SUPER ADMIN)
        if (usuarioRepository.findByLogin("superadmin").isEmpty()) {
            Tercero terJuan = crearTerceroBasic("1010", "Juan", "Admin", "juan@nodo.com");
            Tercero terEmpNodo = crearTerceroBasic("9001", "Sistemas", "Nodo", "contacto@nodo.com");
            Empresa empNodo = crearEmpresaBasic(terEmpNodo, "SISTEMAS NODO", giroZapa);
            crearUsuarioBasic("superadmin", "admin123", terJuan, empNodo, superRol);
        }

        // 5. ESTRUCTURA DE DIEGO (BILLARES DIEGO)
        if (usuarioRepository.findByLogin("diego_admin").isEmpty()) {
            Tercero terDiego = crearTerceroBasic("2020", "Diego", "Cliente", "diego@billares.com");
            Tercero terEmpDiego = crearTerceroBasic("8002", "Billares", "Diego", "ventas@billaresdiego.com");
            
            Empresa empDiego = crearEmpresaBasic(terEmpDiego, "BILLARES DIEGO", giroBillar);
            vincularPrograma(empDiego, progInv);

            crearUsuarioBasic("diego_admin", "diego123", terDiego, empDiego, adminRol);

            // 6. CONTROL DE DISPOSITIVOS (SUSCRIPCIÓN)
            SuscripcionPrograma subDiego = new SuscripcionPrograma();
            subDiego.setEmpresa(empDiego);
            subDiego.setPrograma(progInv);
            subDiego.setMaxDispositivos(5);
            subDiego.setDispositivosActivos(1);
            subDiego.setActivo(true);
            suscripcionProgramaRepository.save(subDiego);

            // Registro de la Tablet de prueba (UUID Hardware de tu tablet motorola)
            TerminalDispositivo tablet1 = new TerminalDispositivo();
            tablet1.setSuscripcion(subDiego);
            tablet1.setUuidHardware("809fca6bebd005e2"); // El UUID que vimos en tus logs
            tablet1.setAlias("Tablet Motorola G84");
            tablet1.setFechaRegistro(LocalDateTime.now());
            tablet1.setBloqueado(false);
            terminalDispositivoRepository.save(tablet1);

            // 7. CREACIÓN DE SLOTS OPERATIVOS (TRABAJADORES PARA LA TABLET)
            // Estos son los que el RecyclerView en Android DEBE mostrar
            crearSlot(empDiego, "MESERO ALEJO", "M1_ALEJO", "1234", opRol);
            crearSlot(empDiego, "CAJERO CARLOS", "C1_CARLOS", "5555", opRol);
            crearSlot(empDiego, "BARTENDER LUCIA", "B1_LUCIA", "0000", opRol);
            crearSlot(empDiego, "MESERO PEDRO", "M2_PEDRO", "4321", opRol);

            System.out.println("-----------------------------------------");
            System.out.println("🚀 PRUEBA COMPLETA LISTA");
            System.out.println("🏢 Empresa: BILLARES DIEGO (ID: " + empDiego.getId() + ")");
            System.out.println("📱 Terminal Motorola Vinculada.");
            System.out.println("👥 4 Slots creados con PINs: 1234, 5555, 0000, 4321.");
            System.out.println("-----------------------------------------");
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void crearSlot(Empresa emp, String alias, String login, String pin, Rol rol) {
        UsuarioOperativo op = new UsuarioOperativo();
        op.setEmpresa(emp);
        op.setAlias(alias);
        op.setLogin(login);
        op.setPassword(passwordEncoder.encode(pin)); // PIN Encriptado
        op.setEstado(EstadoUsuario.ACTIVO);
        op.setRol(rol);
        op.setFechaCreacion(LocalDateTime.now());
        usuarioOperativoRepository.save(op);
    }

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
        e.setGiroNegocio(gn);
        e.setActivo(true);
        return empresaRepository.save(e);
    }

    private Usuario crearUsuarioBasic(String login, String pass, Tercero t, Empresa e, Rol r) {
        Usuario u = new Usuario();
        u.setLogin(login);
        u.setPassword(passwordEncoder.encode(pass));
        System.out.println("VALIDAR----");
        System.out.println(passwordEncoder.encode(pass));
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