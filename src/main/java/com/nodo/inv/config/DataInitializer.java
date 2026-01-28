package com.nodo.inv.config;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

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

    // Nuevos repositorios para productos y parámetros
    private final ClaseRepository claseRepository;
    private final EstructuraRepository estructuraRepository;
    private final UnidadRepository unidadRepository;
    private final ProductoRepository productoRepository;

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
            tablet1.setUuidHardware("809fca6bebd005e2");
            tablet1.setAlias("Tablet Motorola G84");
            tablet1.setFechaRegistro(LocalDateTime.now());
            tablet1.setBloqueado(false);
            terminalDispositivoRepository.save(tablet1);

            // 7. CREACIÓN DE SLOTS OPERATIVOS
            crearSlot(empDiego, "MESERO ALEJO", "M1_ALEJO", "1234", opRol);
            crearSlot(empDiego, "CAJERO CARLOS", "C1_CARLOS", "5555", opRol);
            crearSlot(empDiego, "BARTENDER LUCIA", "B1_LUCIA", "0000", opRol);
            crearSlot(empDiego, "MESERO PEDRO", "M2_PEDRO", "4321", opRol);

            // 8. MOTOR DE PARÁMETROS: CLASE -> ESTRUCTURA -> UNIDAD
            Clase claseInv = checkAndCreateClase("INVENTARIO", "INV");

            // Estructura para Categorías
            Estructura estCat = checkAndCreateEstructura(claseInv, "CATEGORIAS DE PRODUCTO", "CAT_PROD");
            Unidad uniBebida = checkAndCreateUnidad(estCat, "BEBIDAS", "BEB");
            Unidad uniComida = checkAndCreateUnidad(estCat, "COMIDAS", "COM");
            Unidad uniLicores = checkAndCreateUnidad(estCat, "LICORES", "LIC");

            // Estructura para Unidades de Medida
            Estructura estMed = checkAndCreateEstructura(claseInv, "UNIDADES DE MEDIDA", "UNI_MED");
            Unidad uniBotella = checkAndCreateUnidad(estMed, "BOTELLA", "BOT");
            Unidad uniPlato = checkAndCreateUnidad(estMed, "PLATO", "PLA");
            Unidad uniTrago = checkAndCreateUnidad(estMed, "TRAGO", "TRA");

            // 9. CATÁLOGO DE PRODUCTOS (Asociados a BILLARES DIEGO)
            crearProducto(empDiego, "P001", "Cerveza Poker 330ml", uniBebida, uniBotella, 3500.0, 5500.0, 100);
            crearProducto(empDiego, "P002", "Cerveza Club Colombia", uniBebida, uniBotella, 4000.0, 6500.0, 80);
            crearProducto(empDiego, "P003", "Empanada de Carne", uniComida, uniPlato, 1200.0, 2500.0, 50);
            crearProducto(empDiego, "P004", "Picada Familiar", uniComida, uniPlato, 25000.0, 45000.0, 20);
            crearProducto(empDiego, "P005", "Aguardiente Antioqueño (Trago)", uniLicores, uniTrago, 5000.0, 12000.0, 40);

            System.out.println("-----------------------------------------");
            System.out.println("🚀 PRUEBA COMPLETA LISTA");
            System.out.println("🏢 Empresa: BILLARES DIEGO (ID: " + empDiego.getId() + ")");
            System.out.println("📦 Catálogo de productos sincronizado.");
            System.out.println("👥 4 Slots creados.");
            System.out.println("-----------------------------------------");
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private Clase checkAndCreateClase(String nombre, String codigo) {
        return claseRepository.findByCodigo(codigo).orElseGet(() -> {
            Clase c = new Clase();
            c.setNombre(nombre);
            c.setCodigo(codigo);
            return claseRepository.save(c);
        });
    }

    private Estructura checkAndCreateEstructura(Clase clase, String nombre, String codigo) {
        return estructuraRepository.findByCodigo(codigo).orElseGet(() -> {
            Estructura e = new Estructura();
            e.setClase(clase);
            e.setNombre(nombre);
            e.setCodigo(codigo);
            return estructuraRepository.save(e);
        });
    }

    private Unidad checkAndCreateUnidad(Estructura est, String nombre, String codigo) {
        return unidadRepository.findByEstructuraId(est.getId()).stream()
                .filter(u -> u.getCodigo().equals(codigo))
                .findFirst()
                .orElseGet(() -> {
                    Unidad u = new Unidad();
                    u.setEstructura(est);
                    u.setNombre(nombre);
                    u.setCodigo(codigo);
                    return unidadRepository.save(u);
                });
    }

    private void crearProducto(Empresa emp, String cod, String nom, Unidad cat, Unidad med, Double costo, Double venta, Integer stock) {
        Producto p = new Producto();
        p.setEmpresa(emp);
        p.setCodigo(cod);
        p.setNombre(nom);
        p.setCategoria(cat);
        p.setUnidadMedida(med);
        p.setPrecioCosto(BigDecimal.valueOf(costo));
        p.setPrecioVenta(BigDecimal.valueOf(venta));
        p.setStockActual(stock);
        p.setStockMinimo(5);
        p.setActivo(true);
        productoRepository.save(p);
    }

    private void crearSlot(Empresa emp, String alias, String login, String pin, Rol rol) {
        UsuarioOperativo op = new UsuarioOperativo();
        op.setEmpresa(emp);
        op.setAlias(alias);
        op.setLogin(login);
        op.setPassword(passwordEncoder.encode(pin));
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