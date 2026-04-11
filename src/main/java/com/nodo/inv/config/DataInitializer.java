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
    private final SuscripcionProgramaRepository suscripcionProgramaRepository;
    private final GiroNegocioRepository giroNegocioRepository;
    private final ClaseRepository claseRepository;
    private final EstructuraRepository estructuraRepository;
    private final UnidadRepository unidadRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // ==========================================
        // 1. MOTOR PARAMÉTRICO (LISTAS DESPLEGABLES)
        // ==========================================
        
        // --- CLASE: PARÁMETROS GLOBALES ---
        Clase claseGlobal = checkAndCreateClase("PARÁMETROS GLOBALES", "GLOBAL", "Configuraciones base transversales a todo el sistema");

        // --- ESTRUCTURAS Y UNIDADES ---
        Estructura estTipId = checkAndCreateEstructura(claseGlobal, "TIPO DE IDENTIFICACIÓN", "TIP_ID");
        Unidad uniCC = checkAndCreateUnidad(estTipId, "Cédula de Ciudadanía", "CC");
        Unidad uniNIT = checkAndCreateUnidad(estTipId, "Número de Identificación Tributaria", "NIT");

        Estructura estTipTer = checkAndCreateEstructura(claseGlobal, "TIPO DE TERCERO", "TIP_TER");
        Unidad uniNatural = checkAndCreateUnidad(estTipTer, "Persona Natural", "NATURAL");
        Unidad uniJuridica = checkAndCreateUnidad(estTipTer, "Persona Jurídica", "JURIDICA");
        
        Estructura estTipItem = checkAndCreateEstructura(claseGlobal, "TIPO DE ÍTEM DE VENTA", "TIP_ITEM_VTA");
        
        // Estas son las "Unidades" que reemplazan a "esApuesta"
        Unidad uniItemProducto = checkAndCreateUnidad(estTipItem, "Producto Físico", "ITEM_PRODUCTO");
        Unidad uniItemServicio = checkAndCreateUnidad(estTipItem, "Servicio General", "ITEM_SERVICIO");
        Unidad uniItemTiempo = checkAndCreateUnidad(estTipItem, "Alquiler / Tiempo de Juego", "ITEM_TIEMPO");
        Unidad uniItemRecargo = checkAndCreateUnidad(estTipItem, "Recargo / Propina", "ITEM_RECARGO");
        Unidad uniItemApuesta = checkAndCreateUnidad(estTipItem, "Apuesta / Duelo", "ITEM_APUESTA");
        
        // ==========================================
        // 2. GIROS DE NEGOCIO, ROLES Y PERMISOS (SaaS)
        // ==========================================
        GiroNegocio giroBillar = checkAndCreateGiro("RESTAURANTE / BILLAR", "REST_BILL", "ARENA_DUELO");
        GiroNegocio giroRetail = checkAndCreateGiro("ZAPATERÍA / RETAIL", "RETAIL", "POS_ESTANDAR");

        Rol superRol = checkAndCreateRol("SUPER", "Administrador Global del Sistema");
        Rol adminRol = checkAndCreateRol("ADMIN", "Propietario / Tenant del Negocio");
        Rol opRol = checkAndCreateRol("OPERATIVO", "Cajero / Mesero / Operador de TPV");
        
        // 🔥 CREACIÓN DE FICHAS DE LEGO (Funcionalidades del Sistema)
        Permiso modInventario = checkAndCreatePermiso("MOD_INVENTARIO", "Módulo de Gestión de Inventarios y Catálogo");
        Permiso modCaja = checkAndCreatePermiso("MOD_CAJA", "Módulo de Punto de Venta y Facturación");
        Permiso modTablets = checkAndCreatePermiso("MOD_TABLETS", "Módulo de Gestión de Dispositivos (Tablets y QR)");
        Permiso modPersonal = checkAndCreatePermiso("MOD_PERSONAL", "Módulo de Gestión de Empleados y Slots");
        Permiso modLiquidacion = checkAndCreatePermiso("MOD_LIQUID_SLOT", "Módulo Avanzado de Liquidación de Nómina y Comisiones");

        // 🔥 CREACIÓN DE PROGRAMAS (Paquetes a vender)
        Programa progInv = checkAndCreatePrograma("Inventario y Catálogo", "INV", "Gestión de stock y productos");
        Programa progPosBasic = checkAndCreatePrograma("Punto de Venta (Básico)", "POS_BASIC", "Caja, comandas y personal básico");
        Programa progPosPro = checkAndCreatePrograma("Punto de Venta (Premium)", "POS_PRO", "Caja, personal y liquidación automática");

        // ==========================================
        // 3. ESTRUCTURA MAESTRA (EL SUPER ADMIN NODO)
        // ==========================================
        if (usuarioRepository.findByLogin("superadmin").isEmpty()) {
            
            // 3.1. Tercero y Empresa Dueña del Software (Tú)
            Tercero terJuan = crearTerceroBasic("101010", "Master", "Admin", "admin@nodo.com", uniCC, uniNatural);
            Tercero terEmpNodo = crearTerceroBasic("900000000", "Sistemas", "Nodo SAS", "contacto@nodo.com", uniNIT, uniJuridica);
            Empresa empNodo = crearEmpresaBasic(terEmpNodo, "NODO MASTER INC.", giroRetail);
            
            // 3.2. Asignamos TODOS los programas al SuperAdmin para que no se le bloquee la pantalla
            vincularPrograma(empNodo, progInv);
            vincularPrograma(empNodo, progPosBasic);
            vincularPrograma(empNodo, progPosPro);

            crearSuscripcion(empNodo, progInv, 999);
            crearSuscripcion(empNodo, progPosPro, 999);

            // 3.3. Creamos tus credenciales de acceso
            crearUsuarioBasic("superadmin", "admin123", terJuan, empNodo, superRol);

            System.out.println("-----------------------------------------");
            System.out.println("🚀 SISTEMA NODO INICIALIZADO LIMPIO");
            System.out.println("👑 Credenciales: superadmin / admin123");
            System.out.println("-----------------------------------------");
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES (Helpers Limpios)
    // ==========================================

    private Clase checkAndCreateClase(String nombre, String codigo, String desc) {
        return claseRepository.findByCodigo(codigo).orElseGet(() -> {
            Clase c = new Clase();
            c.setNombre(nombre);
            c.setCodigo(codigo);
            c.setDescripcion(desc);
            c.setActivo(true);
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
                    u.setEsGlobal(true);
                    return unidadRepository.save(u);
                });
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

    private Rol checkAndCreateRol(String nombre, String desc) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            r.setDescripcion(desc);
            r.setActivo(true);
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

    private Programa checkAndCreatePrograma(String nom, String cod, String desc) {
        return programaRepository.findByCodigo(cod).orElseGet(() -> {
            Programa p = new Programa();
            p.setNombre(nom);
            p.setCodigo(cod);
            p.setDescripcion(desc);
            p.setActivo(true);
            p.setVersion("1.0.0");
            return programaRepository.save(p);
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

    private void vincularPrograma(Empresa e, Programa p) {
        EmpresaPrograma ep = new EmpresaPrograma();
        ep.setEmpresa(e);
        ep.setPrograma(p);
        ep.setEstado(true);
        ep.setFechaActivacion(LocalDateTime.now());
        empresaProgramaRepository.save(ep);
    }

    private void crearSuscripcion(Empresa emp, Programa prog, int cupos) {
        SuscripcionPrograma sub = new SuscripcionPrograma();
        sub.setEmpresa(emp);
        sub.setPrograma(prog);
        sub.setMaxDispositivos(cupos);
        sub.setDispositivosActivos(0);
        sub.setActivo(true);
        suscripcionProgramaRepository.save(sub);
    }
}