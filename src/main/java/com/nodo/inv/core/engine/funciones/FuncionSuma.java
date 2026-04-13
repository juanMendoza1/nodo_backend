package com.nodo.inv.core.engine.funciones;

import org.springframework.stereotype.Component;

import com.nodo.inv.core.engine.Funcion;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FuncionSuma implements Funcion {

    @Override
    public String getIdentificador() {
        return "F_SUMA";
    }

    @Override
    public BigDecimal ejecutar(List<BigDecimal> parametros) {
        // Si por error envían F_SUMA() vacío, retorna 0
        if (parametros == null || parametros.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Suma todos los elementos de la lista
        return parametros.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}