package com.nodo.inv.nomina.dto;

import java.util.List;

public record LiquidacionDTO(
	    Long id,
	    String codigo,
	    String nombre,
	    Long programaId,
	    Long tipoDocumentoId,
	    List<ConceptoLiquidacionDTO> conceptosRelacionados
	) {}

	
