package com.nodo.inv.core.service;

import com.nodo.inv.core.engine.Funcion;
import com.nodo.inv.core.entity.Concepto;
import com.nodo.inv.core.entity.ConceptoRelacionado;
import com.nodo.inv.core.repository.ConceptoRelacionadoRepository;
import com.nodo.inv.core.repository.ConceptoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConceptoService {

    private final ConceptoRepository conceptoRepository;
    private final ConceptoRelacionadoRepository relacionadoRepository;

    // 🔥 INYECTAMOS TODAS LAS FUNCIONES NATIVAS DE SPRING BOOT
    private final List<Funcion> funcionesDelMotor; 
    private Set<String> identificadoresDeFunciones;

    /**
     * Se ejecuta automáticamente al arrancar la aplicación.
     * Toma todas las clases que implementen "Funcion" y guarda sus nombres (Ej: "F_SUMA")
     * en un Set de memoria ultrarrápida O(1).
     */
    @PostConstruct
    public void inicializarFuncionesNativas() {
        identificadoresDeFunciones = funcionesDelMotor.stream()
                .map(Funcion::getIdentificador)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Concepto guardarConcepto(Concepto concepto) {
        // 1. Validar unicidad del código
        validarCodigoUnico(concepto);

        // 2. Guardamos primero para que adquiera ID
        Concepto conceptoGuardado = conceptoRepository.save(concepto);

        // 3. Validar dependencias si es fórmula
        if ("FORMULA".equalsIgnoreCase(conceptoGuardado.getTipoCalculo()) && conceptoGuardado.getFormula() != null) {
            
            Set<String> codigosVariables = extraerCodigosDeFormula(conceptoGuardado.getFormula());
            validarYRegistrarDependencias(conceptoGuardado, codigosVariables);
            
        } else {
            // Si ya no es fórmula, borramos dependencias huérfanas
            relacionadoRepository.deleteAll(relacionadoRepository.findByConceptoPadreId(conceptoGuardado.getId()));
        }

        return conceptoGuardado;
    }

    private void validarCodigoUnico(Concepto concepto) {
        conceptoRepository.findByCodigo(concepto.getCodigo()).ifPresent(existente -> {
            if (!existente.getId().equals(concepto.getId())) {
                throw new RuntimeException("El código de concepto '" + concepto.getCodigo() + "' ya existe.");
            }
        });
    }

    private Set<String> extraerCodigosDeFormula(String formula) {
        Set<String> codigos = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher matcher = pattern.matcher(formula);
        
        while (matcher.find()) {
            String palabra = matcher.group();
            
            // 🔥 LA LÓGICA PRO: 
            // Verificamos matemáticamente que no sea un número.
            // Y verificamos que NO exista dentro del catálogo dinámico de funciones de la Interfaz.
            if (!palabra.matches("\\d+") && !identificadoresDeFunciones.contains(palabra)) {
                codigos.add(palabra);
            }
        }
        return codigos;
    }

    private void validarYRegistrarDependencias(Concepto padre, Set<String> codigosHijos) {
        relacionadoRepository.deleteAll(relacionadoRepository.findByConceptoPadreId(padre.getId()));

        for (String codigoHijo : codigosHijos) {
            Concepto hijo = conceptoRepository.findByCodigo(codigoHijo)
                .orElseThrow(() -> new RuntimeException("Error en la fórmula: La variable '" + codigoHijo + "' no es un concepto válido ni una función nativa del sistema."));

            if (hijo.getCodigo().equals(padre.getCodigo())) {
                throw new RuntimeException("Referencia circular: El concepto no puede depender de sí mismo.");
            }

            chequearCicloRecursivo(padre.getCodigo(), hijo);

            ConceptoRelacionado relacion = new ConceptoRelacionado();
            relacion.setConceptoPadre(padre);
            relacion.setConceptoHijo(hijo);
            relacion.setTipoRelacion("FORMULA_VAR");
            relacionadoRepository.save(relacion);
        }
    }

    private void chequearCicloRecursivo(String codigoOriginal, Concepto conceptoActual) {
        if ("FORMULA".equalsIgnoreCase(conceptoActual.getTipoCalculo()) && conceptoActual.getFormula() != null) {
            Set<String> hijosDelHijo = extraerCodigosDeFormula(conceptoActual.getFormula());
            
            if (hijosDelHijo.contains(codigoOriginal)) {
                throw new RuntimeException("¡Ciclo detectado! '" + codigoOriginal + "' depende de un concepto que a su vez vuelve a él.");
            }
            
            for (String h : hijosDelHijo) {
                conceptoRepository.findByCodigo(h).ifPresent(next -> chequearCicloRecursivo(codigoOriginal, next));
            }
        }
    }
}