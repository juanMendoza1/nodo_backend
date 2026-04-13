package com.nodo.inv.core.engine.funciones;

import org.springframework.stereotype.Component;

import com.nodo.inv.core.engine.Funcion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class FuncionPromedio implements Funcion {

    @Override
    public String getIdentificador() {
        return "F_PROMEDIO";
    }

    @Override
    public BigDecimal ejecutar(List<BigDecimal> parametros) {
        if (parametros == null || parametros.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 1. Sumamos todo
        BigDecimal sumaTotal = parametros.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Dividimos por la cantidad de elementos (Redondeando a 4 decimales por seguridad)
        BigDecimal cantidad = new BigDecimal(parametros.size());
        
        return sumaTotal.divide(cantidad, 4, RoundingMode.HALF_UP);
    }
}