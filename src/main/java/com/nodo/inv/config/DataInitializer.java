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
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TerceroRepository terceroRepository;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    // 🔥 Ya no inyectamos ProgramaRepository ni Suscripciones aquí
    private final PermisoRepository permisoRepository;
    private final GiroNegocioRepository giroNegocioRepository;
    private final ClaseRepository claseRepository;
    private final EstructuraRepository estructuraRepository;
    private final UnidadRepository unidadRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    
    // 🔥 INYECTAMOS EL NUEVO REPOSITORIO DE DOMINIOS
    private final DominioOperativoRepository dominioOperativoRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // ==========================================
        // 1. MOTOR PARAMÉTRICO (LISTAS DESPLEGABLES)
        // ==========================================
        
        Clase claseGlobal = checkAndCreateClase("PARÁMETROS GLOBALES", "GLOBAL", "Configuraciones base transversales a todo el sistema");

        Estructura estTipId = checkAndCreateEstructura(claseGlobal, "TIPO DE IDENTIFICACIÓN", "TIP_ID");
        Unidad uniCC = checkAndCreateUnidad(estTipId, "Cédula de Ciudadanía", "CC");
        Unidad uniNIT = checkAndCreateUnidad(estTipId, "Número de Identificación Tributaria", "NIT");

        Estructura estTipTer = checkAndCreateEstructura(claseGlobal, "TIPO DE TERCERO", "TIP_TER");
        Unidad uniNatural = checkAndCreateUnidad(estTipTer, "Persona Natural", "NATURAL");
        Unidad uniJuridica = checkAndCreateUnidad(estTipTer, "Persona Jurídica", "JURIDICA");
        
        Estructura estTipItem = checkAndCreateEstructura(claseGlobal, "TIPO DE ÍTEM DE VENTA", "TIP_ITEM_VTA");
        checkAndCreateUnidad(estTipItem, "Producto Físico", "ITEM_PRODUCTO");
        checkAndCreateUnidad(estTipItem, "Servicio General", "ITEM_SERVICIO");
        checkAndCreateUnidad(estTipItem, "Alquiler / Tiempo de Juego", "ITEM_TIEMPO");
        checkAndCreateUnidad(estTipItem, "Recargo / Propina", "ITEM_RECARGO");
        checkAndCreateUnidad(estTipItem, "Apuesta / Duelo", "ITEM_APUESTA");
        
        // ==========================================
        // 2. DOMINIOS OPERATIVOS (MOTORES DE BASE DE DATOS)
        // ==========================================
        checkAndCreateDominio("RETAIL", "Ventas, POS y Retail", "inv_", "retailSyncProcessor");
        checkAndCreateDominio("SERVICIOS_PUBLICOS", "Servicios Públicos (Agua, Luz)", "sp_", "utilitiesSyncProcessor");
        checkAndCreateDominio("HOSPITALIDAD", "Hotelería y Reservas", "htl_", "hospitalitySyncProcessor");
        checkAndCreateDominio("AGRO", "Agropecuario / Fincas", "agro_", "agroSyncProcessor");

        // ==========================================
        // 3. GIROS DE NEGOCIO, ROLES Y MÓDULOS (SaaS)
        // ==========================================
        GiroNegocio giroBillar = checkAndCreateGiro("RESTAURANTE / BILLAR", "REST_BILL", "ARENA_DUELO");
        GiroNegocio giroRetail = checkAndCreateGiro("ZAPATERÍA / RETAIL", "RETAIL", "POS_ESTANDAR");

        Rol superRol = checkAndCreateRol("SUPER", "Administrador Global del Sistema");
        Rol adminRol = checkAndCreateRol("ADMIN", "Propietario / Tenant del Negocio");
        Rol opRol = checkAndCreateRol("OPERATIVO", "Cajero / Mesero / Operador de TPV");
        
        // Permisos (Fichas de Lego / Módulos SaaS)
        checkAndCreatePermiso("MOD_INVENTARIO", "Módulo de Gestión de Inventarios y Catálogo");
        checkAndCreatePermiso("MOD_CAJA", "Módulo de Punto de Venta y Facturación");
        checkAndCreatePermiso("MOD_TABLETS", "Módulo de Gestión de Dispositivos (Tablets y QR)");
        checkAndCreatePermiso("MOD_PERSONAL", "Módulo de Gestión de Empleados y Slots");
        checkAndCreatePermiso("MOD_LIQUID_SLOT", "Módulo Avanzado de Liquidación de Nómina y Comisiones");

        // 🔥 LA CREACIÓN DE PROGRAMAS Y SUSCRIPCIONES HA SIDO ELIMINADA DE AQUÍ
        // (Ahora se hace dinámicamente desde el SuperAdminDashboard)

        // ==========================================
        // 4. ESTRUCTURA MAESTRA (EL SUPER ADMIN NODO)
        // ==========================================
        if (usuarioRepository.findByLogin("superadmin").isEmpty()) {
            
            // Tercero y Empresa Dueña del Software (Tú)
            Tercero terJuan = crearTerceroBasic("101010", "Master", "Admin", "admin@nodo.com", uniCC, uniNatural);
            Tercero terEmpNodo = crearTerceroBasic("900000000", "Sistemas", "Nodo SAS", "contacto@nodo.com", uniNIT, uniJuridica);
            Empresa empNodo = crearEmpresaBasic(terEmpNodo, "NODO MASTER INC.", giroRetail);
            
            // Credenciales SuperAdmin
            crearUsuarioBasic("superadmin", "admin123", terJuan, empNodo, superRol);
            
            // ==========================================
            // 5. TIPOS DE DOCUMENTO Y FLUJO CONTABLE
            // ==========================================
            TipoDocumento fv = checkAndCreateTipoDocumento("FV", "Factura de Venta", "SUMA");
            TipoDocumento ce = checkAndCreateTipoDocumento("CE", "Comprobante de Egreso", "RESTA"); 
            TipoDocumento rc = checkAndCreateTipoDocumento("RC", "Recibo de Caja", "RESTA"); 
            TipoDocumento nc = checkAndCreateTipoDocumento("NC", "Nota Crédito", "RESTA");
            TipoDocumento nd = checkAndCreateTipoDocumento("ND", "Nota Débito", "SUMA");

            // Reglas de Flujo de Documentos
            if (fv.getDocumentosPermitidos().isEmpty()) {
                fv.setDocumentosPermitidos(new HashSet<>(Set.of(rc, nc, nd)));
                tipoDocumentoRepository.save(fv);
            }
            if (ce.getDocumentosPermitidos().isEmpty()) {
                ce.setDocumentosPermitidos(new HashSet<>(Set.of(nc, nd)));
                tipoDocumentoRepository.save(ce);
            }

            System.out.println("-----------------------------------------");
            System.out.println("🚀 NODO INSTALLER: BASE DE DATOS INICIALIZADA (CLEAN MODE)");
            System.out.println("👑 Credenciales Master: superadmin / admin123");
            System.out.println("👉 ¡Listo para crear Programas SaaS desde la Interfaz Web!");
            System.out.println("-----------------------------------------");
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES (Helpers)
    // ==========================================

    private DominioOperativo checkAndCreateDominio(String codigo, String nombre, String prefijo, String bean) {
        return dominioOperativoRepository.findByCodigo(codigo).orElseGet(() -> {
            DominioOperativo dom = new DominioOperativo();
            dom.setCodigo(codigo);
            dom.setNombre(nombre);
            dom.setPrefijoTablas(prefijo);
            dom.setServiceProcessorBean(bean);
            dom.setActivo(true);
            return dominioOperativoRepository.save(dom);
        });
    }

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

    private TipoDocumento checkAndCreateTipoDocumento(String cod, String nombre, String naturaleza) {
        return tipoDocumentoRepository.findByCodigo(cod).orElseGet(() -> {
            TipoDocumento td = new TipoDocumento();
            td.setCodigo(cod);
            td.setNombre(nombre);
            td.setNaturaleza(naturaleza);
            td.setActivo(true);
            return tipoDocumentoRepository.save(td);
        });
    }
}