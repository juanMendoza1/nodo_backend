package com.nodo.inv.core.processor;

import java.util.Map;

// EL CONTRATO: Todo especialista que se respete debe saber hacer esto.
public interface OperacionDomainProcessor {
    
    /**
     * @param empresaId El tenant al que pertenece la data.
     * @param payload La "caja negra" con los datos crudos que mandó la tablet.
     */
    void procesarSincronizacion(Long empresaId, Map<String, Object> payload);
    
}