package com.nodo.inv.service;

import com.nodo.inv.entity.Concepto;
import com.nodo.inv.entity.ConceptoRelacionado;
import com.nodo.inv.repository.ConceptoRepository;
import com.nodo.inv.repository.ConceptoRelacionadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Concepto guardarConcepto(Concepto concepto) {
        // 1. Validar unicidad del código para la empresa/programa
        validarCodigoUnico(concepto);

        // 2. Si es una fórmula, validamos el árbol de dependencias
        if ("FORMULA".equalsIgnoreCase(concepto.getTipoCalculo()) && concepto.getFormula() != null) {
            
            // Extraemos los códigos (ej: "CERV", "IVA") usando Regex
            Set<String> codigosVariables = extraerCodigosDeFormula(concepto.getFormula());
            
            // Verificamos que no existan ciclos (A -> B -> A)
            validarYRegistrarDependencias(concepto, codigosVariables);
        }

        return conceptoRepository.save(concepto);
    }

    private void validarCodigoUnico(Concepto concepto) {
        // Buscamos si ya existe ese código en el mismo contexto (Empresa/Programa)
        conceptoRepository.findByCodigo(concepto.getCodigo()).ifPresent(existente -> {
            if (!existente.getId().equals(concepto.getId())) {
                throw new RuntimeException("El código de concepto '" + concepto.getCodigo() + "' ya existe.");
            }
        });
    }

    private Set<String> extraerCodigosDeFormula(String formula) {
        Set<String> codigos = new HashSet<>();
        // Buscamos palabras que representen los códigos únicos de los conceptos
        Pattern pattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher matcher = pattern.matcher(formula);
        while (matcher.find()) {
            codigos.add(matcher.group());
        }
        return codigos;
    }

    private void validarYRegistrarDependencias(Concepto padre, Set<String> codigosHijos) {
        // Limpiamos relaciones previas en la tabla core
        relacionadoRepository.deleteAll(relacionadoRepository.findByConceptoPadreId(padre.getId()));

        for (String codigoHijo : codigosHijos) {
            Concepto hijo = conceptoRepository.findByCodigo(codigoHijo)
                .orElseThrow(() -> new RuntimeException("Concepto hijo '" + codigoHijo + "' no encontrado."));

            // Validar que no sea él mismo
            if (hijo.getCodigo().equals(padre.getCodigo())) {
                throw new RuntimeException("Referencia circular: El concepto no puede depender de sí mismo.");
            }

            // Validar recursivamente el árbol (Matryoshka)
            chequearCicloRecursivo(padre.getCodigo(), hijo);

            // Registrar en core_conceptos_relacionados para auditoría
            ConceptoRelacionado relacion = new ConceptoRelacionado();
            relacion.setConceptoPadre(padre);
            relacion.setConceptoHijo(hijo);
            relacion.setTipoRelacion("FORMULA_VAR");
            relacionadoRepository.save(relacion);
        }
    }

    private void chequearCicloRecursivo(String codigoOriginal, Concepto conceptoActual) {
        // Si el concepto actual es una fórmula, revisamos sus hijos
        if ("FORMULA".equalsIgnoreCase(conceptoActual.getTipoCalculo())) {
            Set<String> hijosDelHijo = extraerCodigosDeFormula(conceptoActual.getFormula());
            if (hijosDelHijo.contains(codigoOriginal)) {
                throw new RuntimeException("¡Ciclo detectado! " + codigoOriginal + " depende de un concepto que a su vez vuelve a él.");
            }
            // Seguimos bajando en el árbol
            for (String h : hijosDelHijo) {
                conceptoRepository.findByCodigo(h).ifPresent(next -> chequearCicloRecursivo(codigoOriginal, next));
            }
        }
    }
}