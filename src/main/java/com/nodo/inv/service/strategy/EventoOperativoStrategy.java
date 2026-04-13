package com.nodo.inv.service.strategy;

import java.util.Map;

import com.nodo.inv.retail.entity.ActividadOperativa;

public interface EventoOperativoStrategy {
    String getTipoEvento();
    void procesar(ActividadOperativa actividad, Map<String, Object> data);
}