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

    // Registro dinámico de todas las funciones (F_SUMA, F_GET_IVA, etc.)
    private final Map<String, Funcion> registroFunciones = new HashMap<>();

    // Evaluador matemático nativo de Spring Boot
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
     * MÉTODO MAESTRO: Calcula toda la liquidación en memoria.
     * @param receta La lista de Conceptos configurada (ordenada por dependencias).
     * @param valoresOperativos Datos crudos (Ej: {"CERV" -> 50000, "HORA_BILLAR" -> 15000}).
     * @return Lista de detalles calculados listos para guardar en DocumentoDetalle.
     */
    public List<LineaDetalle> ejecutarLiquidacion(List<ConceptoLiquidacion> receta, Map<String, BigDecimal> valoresOperativos) {
        
        List<LineaDetalle> detallesCalculados = new ArrayList<>();
        Map<String, BigDecimal> contextoMemoria = new HashMap<>(); // Memoria temporal del motor

        for (ConceptoLiquidacion paso : receta) {
            Concepto concepto = paso.getConcepto();
            BigDecimal resultado = BigDecimal.ZERO;

            // 1. Si el concepto es una Función Auxiliar pura en base de datos, no genera cobro, lo saltamos.
            if (Boolean.TRUE.equals(concepto.getEsFuncion())) {
                continue; 
            }

            // 2. CALCULAR SEGÚN EL TIPO
            switch (concepto.getTipoCalculo().toUpperCase()) {
                case "ESTATICO":
                    resultado = concepto.getValorFijo() != null ? concepto.getValorFijo() : BigDecimal.ZERO;
                    break;
                
                case "DINAMICO":
                    resultado = valoresOperativos.getOrDefault(concepto.getCodigo(), BigDecimal.ZERO);
                    break;
                
                case "FORMULA":
                    if (concepto.getFormula() != null && !concepto.getFormula().isBlank()) {
                        resultado = resolverFormula(concepto.getFormula(), contextoMemoria);
                    }
                    break;
                
                default:
                    log.warn("Tipo de cálculo desconocido '{}' en concepto '{}'", concepto.getTipoCalculo(), concepto.getCodigo());
            }

            // 3. GUARDAR RESULTADO EN MEMORIA (Para que los conceptos que dependen de este lo encuentren)
            contextoMemoria.put(concepto.getCodigo(), resultado);

            // 4. ARMAR LÍNEA DEL DOCUMENTO (Ignoramos valores en 0 o negativos para no ensuciar la factura)
            if (resultado.compareTo(BigDecimal.ZERO) > 0) {
                // Si es recaudable, el saldo y valor real es el total. Si no, es 0.
                BigDecimal valorReal = concepto.getEsRecaudable() ? resultado : BigDecimal.ZERO;
                
                detallesCalculados.add(new LineaDetalle(
                		concepto.getCodigo(),
                        concepto.getNombre(),
                        BigDecimal.ONE, // Por defecto consolidado
                        resultado,      // valor_total
                        valorReal       // saldo
                ));
            }
        }

        return detallesCalculados;
    }

    /**
     * PROCESADOR DE FÓRMULAS: Transforma texto en dinero real.
     */
    private BigDecimal resolverFormula(String formulaOriginal, Map<String, BigDecimal> contextoMemoria) {
        String formulaProcesada = formulaOriginal;

        // PASO A: RESOLVER FUNCIONES (Ej: F_SUMA(CERV, 5000) o F_GET_IVA() )
        // Busca funciones que empiecen con F_ seguidas de paréntesis con 0 o más parámetros
        Pattern patternFuncion = Pattern.compile("(F_[A-Z_]+)\\(([^)]*)\\)");
        Matcher matcherFuncion = patternFuncion.matcher(formulaProcesada);

        while (matcherFuncion.find()) {
            String matchCompleto = matcherFuncion.group(0); 
            String nombreFuncion = matcherFuncion.group(1); 
            String parametrosCrudos = matcherFuncion.group(2); 

            Funcion funcion = registroFunciones.get(nombreFuncion);
            if (funcion != null) {
                List<BigDecimal> valoresParametros = extraerValoresParametros(parametrosCrudos, contextoMemoria);
                BigDecimal resultadoFuncion = funcion.ejecutar(valoresParametros);
                
                // Reemplaza el texto de la función por el número resultante
                formulaProcesada = formulaProcesada.replace(matchCompleto, resultadoFuncion.toPlainString());
            } else {
                throw new IllegalArgumentException("La función '" + nombreFuncion + "' no está registrada en el motor.");
            }
        }

        // PASO B: REEMPLAZAR VARIABLES NORMALES POR NÚMEROS (Ej: CERV -> 50000)
        Pattern patternVariable = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher matcherVariable = patternVariable.matcher(formulaProcesada);
        
        while (matcherVariable.find()) {
            String variable = matcherVariable.group();
            
            // Si es un número duro o si empieza por F_ (y quedó huérfano), lo ignoramos
            if (!variable.matches("\\d+") && !variable.startsWith("F_")) {
                BigDecimal valor = contextoMemoria.getOrDefault(variable, BigDecimal.ZERO);
                formulaProcesada = formulaProcesada.replace(variable, valor.toPlainString());
            }
        }

        // PASO C: EJECUTAR MATEMÁTICAS PURAS
        return evaluarMatematica(formulaProcesada);
    }

    /**
     * EXTRAE PARÁMETROS: Traduce "CERV, 0.19" a una lista real [50000, 0.19]
     */
    private List<BigDecimal> extraerValoresParametros(String parametrosCrudos, Map<String, BigDecimal> contexto) {
        if (parametrosCrudos == null || parametrosCrudos.trim().isEmpty()) {
            return new ArrayList<>(); // Soporta funciones de 0 parámetros como F_GET_IVA()
        }

        return Arrays.stream(parametrosCrudos.split(","))
                .map(String::trim)
                .map(parametro -> {
                    // Si el parámetro ya es un número duro (Ej: "0.19" o "500"), lo parsea directo
                    if (parametro.matches("-?\\d+(\\.\\d+)?")) {
                        return new BigDecimal(parametro);
                    }
                    // Si es texto (Variable), la busca en el contexto de memoria
                    return contexto.getOrDefault(parametro, BigDecimal.ZERO);
                })
                .toList();
    }

    /**
     * EVALUADOR NATIVO: Resuelve el texto final matemático usando SpEL (Spring Expression Language)
     */
    private BigDecimal evaluarMatematica(String expresionMatematica) {
        try {
            // SpEL calcula "(50000 * 0.19) + 4000" y devuelve un número
            Number resultado = mathParser.parseExpression(expresionMatematica).getValue(mathContext, Number.class);
            if (resultado != null) {
                // Lo convertimos a BigDecimal con 2 decimales para precisión financiera
                return new BigDecimal(resultado.toString()).setScale(2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error evaluando expresión matemática final: '{}'", expresionMatematica, e);
            throw new RuntimeException("Error matemático en la fórmula: " + expresionMatematica);
        }
    }
}