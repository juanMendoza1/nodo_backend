package com.nodo.inv.core.engine;

import com.nodo.inv.core.dto.DocumentoDTO.LineaDetalle;
import com.nodo.inv.core.entity.Concepto;
import com.nodo.inv.core.entity.ConceptoLiquidacion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class LiquidacionEngine {

    private final Map<String, Funcion> registroFunciones = new HashMap<>();
    private final ExpressionParser mathParser = new SpelExpressionParser();
    private final StandardEvaluationContext mathContext = new StandardEvaluationContext();

    @Autowired
    public LiquidacionEngine(List<Funcion> funcionesDisponibles) {
        for (Funcion funcion : funcionesDisponibles) {
            registroFunciones.put(funcion.getIdentificador(), funcion);
        }
        log.info("LiquidacionEngine inicializado con {} funciones cargadas.", registroFunciones.size());
    }

    /**
     * MÉTODO MAESTRO: Calcula toda la liquidación bajo demanda (Inteligente).
     */
    public List<LineaDetalle> ejecutarLiquidacion(List<ConceptoLiquidacion> receta, Map<String, BigDecimal> valoresOperativos) {
        
        List<LineaDetalle> detallesCalculados = new ArrayList<>();
        
        // 1. Armamos un Diccionario rápido para encontrar cualquier concepto al instante
        Map<String, Concepto> diccionarioConceptos = new LinkedHashMap<>();
        for (ConceptoLiquidacion paso : receta) {
            diccionarioConceptos.put(paso.getConcepto().getCodigo(), paso.getConcepto());
        }

        // 2. Memoria compartida y Control de Ciclos Infinitos
        Map<String, BigDecimal> memoria = new HashMap<>();
        Set<String> enProceso = new HashSet<>();

        // 3. Evaluamos cada concepto de la receta
        for (Concepto concepto : diccionarioConceptos.values()) {
            
            // 🔥 LA MAGIA: Resolvemos el concepto (El motor buscará sus dependencias automáticamente si las necesita)
            BigDecimal resultado = resolverConcepto(concepto.getCodigo(), diccionarioConceptos, valoresOperativos, memoria, enProceso);

            // 4. ARMAR LÍNEA DEL DOCUMENTO (Solo si el resultado es mayor a cero)
            if (resultado.compareTo(BigDecimal.ZERO) > 0) {
                
                // 🔥 REGLA DE ORO CONTABLE: Solo los "Recaudables" afectan el saldo y el valor real de la factura
                boolean esRecaudable = Boolean.TRUE.equals(concepto.getEsRecaudable());
                BigDecimal valorReal = esRecaudable ? resultado : BigDecimal.ZERO;
                
                // Extraemos la naturaleza paramétrica (Por defecto SUMA para evitar nulos)
                String naturaleza = concepto.getNaturaleza() != null ? concepto.getNaturaleza() : "SUMA";
                
                detallesCalculados.add(new LineaDetalle(
                        concepto.getCodigo(),
                        concepto.getNombre(),
                        BigDecimal.ONE, // Cantidad por defecto
                        resultado,      // Valor Total (Siempre se muestra la huella del cálculo)
                        valorReal,      // Saldo / Valor Real (Si no es recaudable, esto va en 0)
                        naturaleza
                ));
            }
        }

        return detallesCalculados;
    }

    /**
     * MOTOR RECURSIVO: Si no sabe el valor de algo, lo calcula y lo guarda en memoria.
     */
    private BigDecimal resolverConcepto(String codigo, Map<String, Concepto> diccionario, Map<String, BigDecimal> operativos, Map<String, BigDecimal> memoria, Set<String> enProceso) {
        
        // ¿Ya lo calculamos antes? Lo sacamos de la memoria al instante
        if (memoria.containsKey(codigo)) {
            return memoria.get(codigo);
        }

        // ¿Estamos atrapados en un bucle? (A necesita B, y B necesita A)
        if (enProceso.contains(codigo)) {
            throw new RuntimeException("Referencia Circular detectada en la matriz matemática. El concepto depende de sí mismo directa o indirectamente: " + codigo);
        }

        Concepto concepto = diccionario.get(codigo);

        // Si el código no es un concepto configurado, miramos si es un dato puro que mandó la tablet
        if (concepto == null) {
            BigDecimal valorCrudo = operativos.getOrDefault(codigo, BigDecimal.ZERO);
            memoria.put(codigo, valorCrudo);
            return valorCrudo;
        }

        // Marcamos que estamos procesando este código
        enProceso.add(codigo);
        BigDecimal resultado = BigDecimal.ZERO;

        // RESOLVEMOS SEGÚN EL TIPO
        switch (concepto.getTipoCalculo().toUpperCase()) {
            case "ESTATICO":
                resultado = concepto.getValorFijo() != null ? concepto.getValorFijo() : BigDecimal.ZERO;
                break;
            
            case "DINAMICO":
                resultado = operativos.getOrDefault(codigo, BigDecimal.ZERO);
                break;
            
            case "FORMULA":
                if (concepto.getFormula() != null && !concepto.getFormula().isBlank()) {
                    // Le pasamos el concepto completo para saber si tiene funciones internamente
                    resultado = resolverFormula(concepto, diccionario, operativos, memoria, enProceso);
                }
                break;
        }

        // Ya terminamos, lo quitamos de la lista de proceso y lo guardamos en la memoria vitalicia
        enProceso.remove(codigo);
        memoria.put(codigo, resultado);
        
        return resultado;
    }

    /**
     * PROCESADOR DE FÓRMULAS: Busca variables y funciones dentro del texto.
     */
    private BigDecimal resolverFormula(Concepto concepto, Map<String, Concepto> diccionario, Map<String, BigDecimal> operativos, Map<String, BigDecimal> memoria, Set<String> enProceso) {
        String formulaProcesada = concepto.getFormula();

        // PASO A: RESOLVER FUNCIONES NATIVAS
        // 🔥 OPTIMIZACIÓN PRO: Solo ejecutamos Regex complejo si el concepto nos avisó que contiene funciones
        if (Boolean.TRUE.equals(concepto.getEsFuncion())) {
            Pattern patternFuncion = Pattern.compile("(F_[A-Z_]+)\\(([^)]*)\\)");
            Matcher matcherFuncion = patternFuncion.matcher(formulaProcesada);

            while (matcherFuncion.find()) {
                String matchCompleto = matcherFuncion.group(0); 
                String nombreFuncion = matcherFuncion.group(1); 
                String parametrosCrudos = matcherFuncion.group(2); 

                Funcion funcion = registroFunciones.get(nombreFuncion);
                if (funcion != null) {
                    List<BigDecimal> valoresParametros = extraerValoresParametros(parametrosCrudos, diccionario, operativos, memoria, enProceso);
                    BigDecimal resultadoFuncion = funcion.ejecutar(valoresParametros);
                    
                    formulaProcesada = formulaProcesada.replace(matchCompleto, resultadoFuncion.toPlainString());
                } else {
                    throw new IllegalArgumentException("La función '" + nombreFuncion + "' no está registrada en el motor.");
                }
            }
        }

        // PASO B: RESOLVER VARIABLES NORMALES (Ej: IVA * VLR_PRG)
        Pattern patternVariable = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher matcherVariable = patternVariable.matcher(formulaProcesada);
        
        while (matcherVariable.find()) {
            String variable = matcherVariable.group();
            
            // Verificamos que no sea un número duro ni una función huérfana
            if (!variable.matches("\\d+") && !variable.startsWith("F_")) {
                
                // 🔥 AQUÍ ESTÁ EL PODER: Mandamos a resolver la variable bajo demanda
                BigDecimal valor = resolverConcepto(variable, diccionario, operativos, memoria, enProceso);
                
                // 🔥 BLINDAJE PRO: Usamos \\b (Word Boundaries) para que "IVA" no sobreescriba "SUBTOTAL_IVA"
                formulaProcesada = formulaProcesada.replaceAll("\\b" + variable + "\\b", valor.toPlainString());
            }
        }

        // PASO C: EJECUTAR MATEMÁTICAS PURAS
        return evaluarMatematica(formulaProcesada);
    }

    /**
     * EXTRAE PARÁMETROS: Traduce "CERV, 0.19" a una lista real [50000, 0.19]
     */
    private List<BigDecimal> extraerValoresParametros(String parametrosCrudos, Map<String, Concepto> diccionario, Map<String, BigDecimal> operativos, Map<String, BigDecimal> memoria, Set<String> enProceso) {
        if (parametrosCrudos == null || parametrosCrudos.trim().isEmpty()) {
            return new ArrayList<>(); 
        }

        return Arrays.stream(parametrosCrudos.split(","))
                .map(String::trim)
                .map(parametro -> {
                    // Si el parámetro ya es un número duro (Ej: "0.19" o "500"), lo parsea directo
                    if (parametro.matches("-?\\d+(\\.\\d+)?")) {
                        return new BigDecimal(parametro);
                    }
                    // Si es texto (Variable), mandamos a resolverlo bajo demanda
                    return resolverConcepto(parametro, diccionario, operativos, memoria, enProceso);
                })
                .toList();
    }

    /**
     * EVALUADOR NATIVO: Resuelve el texto final matemático usando SpEL (Spring Expression Language)
     */
    private BigDecimal evaluarMatematica(String expresionMatematica) {
        try {
            Number resultado = mathParser.parseExpression(expresionMatematica).getValue(mathContext, Number.class);
            if (resultado != null) {
                return new BigDecimal(resultado.toString()).setScale(2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error evaluando expresión matemática final: '{}'", expresionMatematica, e);
            throw new RuntimeException("Error matemático en la fórmula: " + expresionMatematica);
        }
    }
}