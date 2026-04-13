package com.nodo.inv.core.service;

import com.nodo.inv.core.entity.Permiso;
import com.nodo.inv.core.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    /**
     * Obtiene la lista de todos los módulos creados en el sistema.
     */
    @Transactional(readOnly = true)
    public List<Permiso> obtenerTodos() {
        return permisoRepository.findAll();
    }

    /**
     * Crea un nuevo Módulo SaaS.
     */
    @Transactional
    public Permiso crear(Permiso permiso) {
        // Blindaje: Aseguramos que el código siempre se guarde en MAYÚSCULAS y sin espacios
        if (permiso.getCodigo() != null) {
            permiso.setCodigo(permiso.getCodigo().toUpperCase().trim().replace(" ", "_"));
        }
        return permisoRepository.save(permiso);
    }

    /**
     * Actualiza los datos básicos de un Módulo SaaS existente.
     */
    @Transactional
    public Permiso actualizar(Long id, Permiso datosNuevos) {
        Permiso permisoDb = permisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Módulo SaaS no encontrado con ID: " + id));

        if (datosNuevos.getCodigo() != null) {
            permisoDb.setCodigo(datosNuevos.getCodigo().toUpperCase().trim().replace(" ", "_"));
        }
        permisoDb.setDescripcion(datosNuevos.getDescripcion());

        return permisoRepository.save(permisoDb);
    }

    /**
     * Elimina un Módulo SaaS.
     * (Nota: Si el módulo ya está asignado a un Programa, la base de datos abortará la eliminación
     * por integridad referencial, lo cual es excelente para evitar romper suscripciones activas).
     */
    @Transactional
    public void eliminar(Long id) {
        if (!permisoRepository.existsById(id)) {
            throw new RuntimeException("Módulo SaaS no encontrado con ID: " + id);
        }
        permisoRepository.deleteById(id);
    }

    // =========================================================================
    // 🔥 EL MOTOR DE DEPENDENCIAS (LA MAGIA DEL EMPAQUETADO)
    // =========================================================================

    /**
     * Actualiza el árbol de dependencias de un módulo.
     * Ejemplo: Si le pasamos el ID de "MOD_TABLETS" y la lista [ID_PERSONAL],
     * el sistema amarrará obligatoriamente el módulo de Personal al de Tablets.
     */
    @Transactional
    public Permiso actualizarDependencias(Long id, List<Long> dependenciasIds) {
        // 1. Buscamos el módulo principal
        Permiso permisoPrincipal = permisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Módulo principal no encontrado con ID: " + id));

        // 2. Si la lista de IDs viene vacía o nula, simplemente limpiamos las dependencias
        if (dependenciasIds == null || dependenciasIds.isEmpty()) {
            permisoPrincipal.getDependencias().clear();
        } else {
            // 3. Buscamos en la BD todos los módulos que correspondan a los IDs enviados por React
            List<Permiso> modulosDependencia = permisoRepository.findAllById(dependenciasIds);
            
            // 4. Reemplazamos la lista actual por la nueva lista (HashSet evita duplicados)
            permisoPrincipal.setDependencias(new HashSet<>(modulosDependencia));
        }

        // 5. Guardamos y Spring JPA actualizará la tabla intermedia 'modulo_dependencia' automáticamente
        return permisoRepository.save(permisoPrincipal);
    }
}