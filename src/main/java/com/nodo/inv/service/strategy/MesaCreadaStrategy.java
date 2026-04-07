// src/main/java/com/nodo/inv/service/strategy/MesaCreadaStrategy.java
package com.nodo.inv.service.strategy;

import com.nodo.inv.entity.ActividadOperativa;
import com.nodo.inv.entity.Mesa;
import com.nodo.inv.repository.MesaRepository;
import com.nodo.inv.repository.UsuarioOperativoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MesaCreadaStrategy implements EventoOperativoStrategy {

    private final MesaRepository mesaRepo;
    private final UsuarioOperativoRepository usuarioOperativoRepo;

    @Override
    public String getTipoEvento() {
        return "MESA_CREADA";
    }

    @Override
    public void procesar(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        
        Mesa mesa = actividad.getMesa();
        mesa.setEstado("DISPONIBLE");
        mesa.setTarifaTiempo(null);
        mesa.setFechaApertura(null);
        mesa.setFechaCierre(null);
        mesa.setTipoJuego(null);
        
        if (data != null && data.containsKey("idUsuarioSlot")) {
            Long idSlot = ((Number) data.get("idUsuarioSlot")).longValue();
            usuarioOperativoRepo.findById(idSlot).ifPresent(mesa::setUsuarioActual);
        } else {
            mesa.setUsuarioActual(null);
        }
        mesaRepo.save(mesa);
    }
}