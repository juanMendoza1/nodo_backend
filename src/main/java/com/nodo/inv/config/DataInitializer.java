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
        
        // ==========================================
        // 1. INYECCIÓN DEL MOTOR PARAMÉTRICO
        // ==========================================
        
        // --- CLASE: PARÁMETROS GLOBALES ---
        Clase claseGlobal = claseRepository.findByCodigo("GLOBAL").orElseGet(() -> {
            Clase c = new Clase();
            c.setCodigo("GLOBAL");
            c.setNombre("PARÁMETROS GLOBALES");
            c.setDescripcion("Configuraciones base transversales a todo el sistema");
            c.setActivo(true);
            return claseRepository.save(c);
        });

        // --- ESTRUCTURA: TIPO DE IDENTIFICACIÓN ---
        Estructura estTipId = estructuraRepository.findByCodigo("TIP_ID").orElseGet(() -> {
            Estructura e = new Estructura();
            e.setCodigo("TIP_ID");
            e.setNombre("TIPO DE IDENTIFICACIÓN");
            e.setClase(claseGlobal);
            return estructuraRepository.save(e);
        });

        // UNIDADES: CC, NIT, CE (Globales)
        Unidad uniCC = checkAndCreateUnidad(estTipId, "Cédula de Ciudadanía", "CC");
        Unidad uniNIT = checkAndCreateUnidad(estTipId, "Número de Identificación Tributaria", "NIT");

        // --- ESTRUCTURA: TIPO DE TERCERO ---
        Estructura estTipTer = estructuraRepository.findByCodigo("TIP_TER").orElseGet(() -> {
            Estructura e = new Estructura();
            e.setCodigo("TIP_TER");
            e.setNombre("TIPO DE TERCERO");
            e.setClase(claseGlobal);
            return estructuraRepository.save(e);
        });

        // UNIDADES: Natural, Jurídica (Globales)
        Unidad uniNatural = checkAndCreateUnidad(estTipTer, "Persona Natural", "NATURAL");
        Unidad uniJuridica = checkAndCreateUnidad(estTipTer, "Persona Jurídica", "JURIDICA");
        
        // ==========================================
        // 2. CREACIÓN DE NEGOCIOS Y ROLES
        // ==========================================
        GiroNegocio giroBillar = checkAndCreateGiro("RESTAURANTE / BILLAR", "REST_BILL", "ARENA_DUELO");
        GiroNegocio giroZapa = checkAndCreateGiro("ZAPATERÍA / RETAIL", "ZAPA", "POS_ESTANDAR");
        GiroNegocio giroSuper = checkAndCreateGiro("SUPERMERCADO", "SUPER_MARKET", "LECTOR_BARRAS");

        Rol superRol = checkAndCreateRol("SUPER");
        Rol adminRol = checkAndCreateRol("ADMIN");
        Rol opRol = checkAndCreateRol("OPERATIVO");
        
        // 🔥 CREACIÓN DE FICHAS DE LEGO (Módulos del Sistema SaaS)
        // Se guardan en base de datos para que el SuperAdmin las asigne a los Programas,
        // pero NO se las asignamos directamente a ningún rol aquí.
        Permiso modInventario = checkAndCreatePermiso("MOD_INVENTARIO", "Módulo de Gestión de Inventarios y Catálogo");
        Permiso modCaja = checkAndCreatePermiso("MOD_CAJA", "Módulo de Punto de Venta y Facturación");
        Permiso modTablets = checkAndCreatePermiso("MOD_TABLETS", "Módulo de Gestión de Dispositivos y QR");
        Permiso modPersonal = checkAndCreatePermiso("MOD_PERSONAL", "Módulo de Gestión de Empleados y Operarios");

        Programa progInv = checkAndCreatePrograma("Inventario", "INV");
        Programa progPos = checkAndCreatePrograma("Punto de Venta", "POS");

        // ==========================================
        // 3. ESTRUCTURA DE JUAN (SUPER ADMIN)
        // ==========================================
        if (usuarioRepository.findByLogin("superadmin").isEmpty()) {
            Tercero terJuan = crearTerceroBasic("1010", "Juan", "Admin", "juan@nodo.com", uniCC, uniNatural);
            Tercero terEmpNodo = crearTerceroBasic("9001", "Sistemas", "Nodo", "contacto@nodo.com", uniNIT, uniJuridica);
            Empresa empNodo = crearEmpresaBasic(terEmpNodo, "SISTEMAS NODO", giroZapa);
            
            vincularPrograma(empNodo, progInv);
            vincularPrograma(empNodo, progPos);

            SuscripcionPrograma subNodoInv = new SuscripcionPrograma();
            subNodoInv.setEmpresa(empNodo);
            subNodoInv.setPrograma(progInv);
            subNodoInv.setMaxDispositivos(10); 
            subNodoInv.setDispositivosActivos(0);
            subNodoInv.setActivo(true);
            suscripcionProgramaRepository.save(subNodoInv);

            crearUsuarioBasic("superadmin", "admin123", terJuan, empNodo, superRol);
        }

        // ==========================================
        // 4. ESTRUCTURA DE DIEGO (BILLARES DIEGO)
        // ==========================================
        if (usuarioRepository.findByLogin("diego_admin").isEmpty()) {
            Tercero terDiego = crearTerceroBasic("2020", "Diego", "Cliente", "diego@billares.com", uniCC, uniNatural);
            Tercero terEmpDiego = crearTerceroBasic("8002", "Billares", "Diego", "ventas@billaresdiego.com", uniNIT, uniJuridica);
            
            Empresa empDiego = crearEmpresaBasic(terEmpDiego, "BILLARES DIEGO", giroBillar);
            
            vincularPrograma(empDiego, progInv);
            vincularPrograma(empDiego, progPos);

            crearUsuarioBasic("diego_admin", "diego123", terDiego, empDiego, adminRol);

            SuscripcionPrograma subDiegoInv = new SuscripcionPrograma();
            subDiegoInv.setEmpresa(empDiego);
            subDiegoInv.setPrograma(progInv);
            subDiegoInv.setMaxDispositivos(5);
            subDiegoInv.setDispositivosActivos(1); 
            subDiegoInv.setActivo(true);
            suscripcionProgramaRepository.save(subDiegoInv);

            SuscripcionPrograma subDiegoPos = new SuscripcionPrograma();
            subDiegoPos.setEmpresa(empDiego);
            subDiegoPos.setPrograma(progPos);
            subDiegoPos.setMaxDispositivos(5);
            subDiegoPos.setDispositivosActivos(0);
            subDiegoPos.setActivo(true);
            suscripcionProgramaRepository.save(subDiegoPos);

            TerminalDispositivo tablet1 = new TerminalDispositivo();
            tablet1.setSuscripcion(subDiegoInv); 
            tablet1.setEmpresa(empDiego);        
            tablet1.setPrograma(progInv);        
            tablet1.setUuidHardware("809fca6bebd005e2");
            tablet1.setAlias("Tablet Motorola G84");
            tablet1.setFechaRegistro(LocalDateTime.now());
            tablet1.setBloqueado(false);
            terminalDispositivoRepository.save(tablet1);

            crearSlot(empDiego, progPos, "MESERO ALEJO", "M1_ALEJO", "1234", opRol);
            crearSlot(empDiego, progPos, "CAJERO CARLOS", "C1_CARLOS", "5555", opRol);
            crearSlot(empDiego, progPos, "BARTENDER LUCIA", "B1_LUCIA", "0000", opRol);
            crearSlot(empDiego, progPos, "MESERO PEDRO", "M2_PEDRO", "4321", opRol);

            Clase claseInv = checkAndCreateClase("INVENTARIO", "INV");

            Estructura estCat = checkAndCreateEstructura(claseInv, "CATEGORIAS DE PRODUCTO", "CAT_PROD");
            Unidad uniBebida = checkAndCreateUnidad(estCat, "BEBIDAS", "BEB");
            Unidad uniComida = checkAndCreateUnidad(estCat, "COMIDAS", "COM");
            Unidad uniLicores = checkAndCreateUnidad(estCat, "LICORES", "LIC");

            Estructura estMed = checkAndCreateEstructura(claseInv, "UNIDADES DE MEDIDA", "UNI_MED");
            Unidad uniBotella = checkAndCreateUnidad(estMed, "BOTELLA", "BOT");
            Unidad uniPlato = checkAndCreateUnidad(estMed, "PLATO", "PLA");
            Unidad uniTrago = checkAndCreateUnidad(estMed, "TRAGO", "TRA");

            crearProducto(empDiego, "P001", "Cerveza Poker 330ml", uniBebida, uniBotella, 3500.0, 5500.0, 100);
            crearProducto(empDiego, "P002", "Cerveza Club Colombia", uniBebida, uniBotella, 4000.0, 6500.0, 80);
            crearProducto(empDiego, "P003", "Empanada de Carne", uniComida, uniPlato, 1200.0, 2500.0, 50);
            crearProducto(empDiego, "P004", "Picada Familiar", uniComida, uniPlato, 25000.0, 45000.0, 20);
            crearProducto(empDiego, "P005", "Aguardiente Antioqueño (Trago)", uniLicores, uniTrago, 5000.0, 12000.0, 40);

            System.out.println("-----------------------------------------");
            System.out.println("🚀 PRUEBA COMPLETA LISTA (INCLUYE POS E INV)");
            System.out.println("🏢 Empresa: BILLARES DIEGO (ID: " + empDiego.getId() + ")");
            System.out.println("-----------------------------------------");
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

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
                    u.setEsGlobal(true); // Se asegura de que nazca como global
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

    private void crearSlot(Empresa emp, Programa prog, String alias, String login, String pin, Rol rol) {
        UsuarioOperativo op = new UsuarioOperativo();
        op.setEmpresa(emp);
        op.setPrograma(prog); 
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

    private Tercero crearTerceroBasic(String doc, String nom, String ape, String mail, Unidad tipId, Unidad tipTer) {
        Tercero t = new Tercero();
        t.setDocumento(doc);
        t.setNombre(nom);
        t.setApellido(ape);
        t.setNombreCompleto(nom + " " + ape);
        t.setCorreo(mail);
        t.setTipoIdentificacion(tipId); 
        t.setTipoTercero(tipTer);       
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