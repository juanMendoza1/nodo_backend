package com.nodo.inv.service.strategy;

import com.nodo.inv.entity.ActividadOperativa;
import java.util.Map;

public interface EventoOperativoStrategy {
    String getTipoEvento();
    void procesar(ActividadOperativa actividad, Map<String, Object> data);
}