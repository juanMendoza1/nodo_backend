package com.nodo.inv.Utils;

public enum EstadoPeriodo {
    EN_ESPERA,  // Meses a futuro
    ABIERTO,    // Mes actual rodando
    LIQUIDANDO, // Bloqueo de seguridad mientras el motor genera facturas
    CERRADO     // Inmutable, facturación terminada
}