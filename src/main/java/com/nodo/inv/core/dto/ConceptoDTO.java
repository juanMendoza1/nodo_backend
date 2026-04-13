package com.nodo.inv.core.dto;

import java.math.BigDecimal;

public record ConceptoDTO(
	    Long id,
	    String codigo,
	    String nombre,
	    String tipoCalculo, // DINAMICO, ESTATICO, FORMULA
	    BigDecimal valorFijo,
	    String formula,
	    Boolean esRecaudable,
	    Boolean financiable,
	    Boolean generaInteres,
	    Boolean aplicaIva,
	    Long estructuraId,
	    Long unidadBaseId,
	    Boolean esGlobal
	) {}