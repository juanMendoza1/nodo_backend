package com.nodo.inv.service.engine;

import java.math.BigDecimal;
import java.util.List;

public interface Funcion {
    // El código que buscará el motor (Ej: "F_SUMA")
    String getIdentificador(); 
    
    // La lógica matemática real
    BigDecimal ejecutar(List<BigDecimal> parametros); 
}