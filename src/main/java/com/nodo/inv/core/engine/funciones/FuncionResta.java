package com.nodo.inv.core.engine.funciones;

import org.springframework.stereotype.Component;

import com.nodo.inv.core.engine.Funcion;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FuncionResta implements Funcion {

    @Override
    public String getIdentificador() {
        return "F_RESTA";
    }

    @Override
    public BigDecimal ejecutar(List<BigDecimal> parametros) {
        if (parametros == null || parametros.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Si solo envían un parámetro F_RESTA(1000), devolvemos el mismo número
        if (parametros.size() == 1) {
            return parametros.get(0);
        }

        // Tomamos el primer valor como base
        BigDecimal resultado = parametros.get(0);

        // Le restamos todos los demás valores siguientes
        for (int i = 1; i < parametros.size(); i++) {
            resultado = resultado.subtract(parametros.get(i));
        }

        return resultado;
    }
}