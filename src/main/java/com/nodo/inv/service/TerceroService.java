package com.nodo.inv.service;

import com.nodo.inv.entity.Tercero;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.EmpresaTercero;
import com.nodo.inv.entity.Usuario;
import com.nodo.inv.repository.TerceroRepository;
import com.nodo.inv.repository.EmpresaTerceroRepository;
import com.nodo.inv.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TerceroService {

    private final TerceroRepository terceroRepository;
    private final EmpresaTerceroRepository empresaTerceroRepository;
    private final EmpresaRepository empresaRepository;

    /**
     * Crea un tercero y genera el vínculo con la empresa actual.
     * Si esGlobal es true, será visible para todos los usuarios del sistema.
     */
    @Transactional
    public Tercero crearTercero(Tercero tercero, Long empresaId, Usuario usuarioActuo, boolean esGlobal) {
        // 1. Validar si ya existe el documento
        if (terceroRepository.existsByDocumento(tercero.getDocumento())) {
            throw new RuntimeException("Ya existe un tercero con el documento: " + tercero.getDocumento());
        }

        // 2. Generar nombre completo automáticamente
        if (tercero.getNombreCompleto() == null) {
            tercero.setNombreCompleto(tercero.getNombre() + " " + tercero.getApellido());
        }

        // 3. Guardar la identidad del Tercero
        Tercero terceroGuardado = terceroRepository.save(tercero);

        // 4. Crear el vínculo en la tabla intermedia EmpresaTercero
        EmpresaTercero vinculo = new EmpresaTercero();
        vinculo.setTercero(terceroGuardado);
        vinculo.setEsGlobal(esGlobal);
        vinculo.setFechaVinculo(LocalDateTime.now());
        vinculo.setCreadoPor(usuarioActuo);

        // Si no es un proveedor global de la plataforma, se amarra a la empresa que lo crea
        if (!esGlobal) {
            Empresa emp = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
            vinculo.setEmpresa(emp);
        }

        empresaTerceroRepository.save(vinculo);

        return terceroGuardado;
    }

    /**
     * Lista los terceros que son visibles para una empresa específica
     * (Sus propios terceros + los globales del sistema).
     */
    @Transactional(readOnly = true)
    public List<Tercero> listarTercerosVisibles(Long empresaId) {
        List<EmpresaTercero> vinculos = empresaTerceroRepository.findVisibleByEmpresa(empresaId);
        
        return vinculos.stream()
                .map(EmpresaTercero::getTercero)
                .collect(Collectors.toList());
    }

    /**
     * Método heredado para listar absolutamente todos (solo para Super Admin)
     */
    public List<Tercero> listarTodos() {
        return terceroRepository.findAll();
    }
}