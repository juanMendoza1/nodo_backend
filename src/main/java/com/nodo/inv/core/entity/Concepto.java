package com.nodo.inv.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import com.nodo.inv.Utils.Naturaleza;

@Data
@Entity
@Table(name = "con_concepto")
public class Concepto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conc_ideregistro")
    private Long id;

    // --- SCOPE MULTI-TENANT Y MÓDULOS ---
    @Column(name = "conc_es_global", nullable = false)
    private Boolean esGlobal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_ideregistro")
    private Programa programa;

    // --- IDENTIFICACIÓN ---
    @Column(name = "conc_codigo", length = 50, nullable = false,unique = true)
    private String codigo; // Ej: "CERV", "IVA_19" -> OJO: Esto se usará en la fórmula

    @Column(name = "conc_nombre", nullable = false, length = 150)
    private String nombre; 

    // --- MOTOR MATEMÁTICO ---
    @Column(name = "conc_tipo_calculo", nullable = false, length = 20)
    private String tipoCalculo; // "DINAMICO" (Viene de la venta), "ESTATICO" (Valor fijo), "FORMULA"

    @Column(name = "conc_valor_fijo", precision = 15, scale = 2)
    private BigDecimal valorFijo; // Si tipoCalculo es ESTATICO

    @Column(name = "conc_formula", length = 500)
    private String formula; // Ej: "(CERV * 0.19)"

    // --- REGLAS FINANCIERAS BÁSICAS ---
    @Column(name = "conc_es_recaudable", nullable = false)
    private Boolean esRecaudable; 

    @Column(name = "conc_financiable", nullable = false)
    private Boolean financiable = false;

    @Column(name = "conc_genera_interes", nullable = false)
    private Boolean generaInteres = false;

    @Column(name = "conc_aplica_iva", nullable = false)
    private Boolean aplicaIva = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "conc_naturaleza", nullable = false)
    private Naturaleza naturaleza;

    // --- AGRUPACIONES PARAMÉTRICAS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "est_ideregistro")
    private Estructura estructuraAgrupadora; // Ej: Etiqueta "LIQUIDACION_NOMINA"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uni_ideregistro")
    private Unidad unidadBase; // Puente opcional con la tabla de parametrización genérica

    // --- ESTADO ---
    @Column(name = "conc_estado")
    private Boolean activo = true;
    
    @Column(name = "conc_es_funcion")
    private Boolean esFuncion = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_ideregistro")
    private Usuario usuario; // Quien lo creó o modificó
    
}