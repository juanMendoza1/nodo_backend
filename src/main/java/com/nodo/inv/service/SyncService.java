package com.nodo.inv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.EventoOperativoDTO;
import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.entity.ActividadOperativa;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.repository.ActividadOperativaRepository;
import com.nodo.inv.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SyncService {

    @Autowired
    private ActividadOperativaRepository actividadRepo;

    @Autowired
    private EmpresaRepository empresaRepo;

    // Para aprovechar tu WebSocketConfig y actualizar el Dashboard web
    @Autowired
    private SimpMessagingTemplate messagingTemplate; 

    @Transactional
    public Map<String, Object> procesarPaquete(SincronizacionPaqueteDTO paquete) {
        int procesados = 0;
        int omitidos = 0;

        // 1. Buscamos la empresa para asegurar el aislamiento SaaS
        Empresa empresa = empresaRepo.findById(paquete.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        ObjectMapper mapper = new ObjectMapper();

        // 2. Iteramos sobre cada evento que mandó la tablet
        for (EventoOperativoDTO evento : paquete.getEventos()) {
            
            // 3. LA REGLA DE ORO TÉCNICA: Prevenir duplicados
            if (actividadRepo.existsByEventoId(evento.getEventoId())) {
                omitidos++;
                continue; // Si ya lo tenemos, lo ignoramos y pasamos al siguiente
            }

            // 4. Guardamos en la "Caja Negra" (Bitácora)
            ActividadOperativa actividad = new ActividadOperativa();
            actividad.setEventoId(evento.getEventoId());
            actividad.setTerminalUuid(paquete.getTerminalUuid());
            actividad.setTipoEvento(evento.getTipoEvento());
            actividad.setFechaDispositivo(evento.getFechaDispositivo());
            actividad.setFechaServidor(LocalDateTime.now());
            actividad.setEmpresa(empresa);
            actividad.setEstadoProcesamiento("PROCESADO");

            try {
                // Convertimos el Map de data flexible a JSON de texto para guardarlo
                String jsonStr = mapper.writeValueAsString(evento.getData());
                actividad.setDetallesJson(jsonStr);
            } catch (JsonProcessingException e) {
                actividad.setDetallesJson("{}");
            }

            actividadRepo.save(actividad);
            procesados++;

            // 5. El "Enrutador" de lógica de negocio (Aquí crecerá tu app)
            ejecutarLogicaDeNegocio(actividad, evento.getData());
        }

        // 6. Notificamos al Dashboard Web en tiempo real vía WebSockets
        if (procesados > 0) {
            String topic = "/topic/admin/updates/" + paquete.getEmpresaId();
            messagingTemplate.convertAndSend(topic, "NUEVA_ACTIVIDAD");
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Paquete recibido correctamente");
        respuesta.put("procesados", procesados);
        respuesta.put("omitidos_por_duplicidad", omitidos);
        
        return respuesta;
    }

    private void ejecutarLogicaDeNegocio(ActividadOperativa actividad, Map<String, Object> data) {
        switch (actividad.getTipoEvento()) {
            case "PEDIDO_NUEVO":
                // Aquí solo notificamos al admin que hay una mesa pidiendo algo
                System.out.println("Mesa registró nuevo pedido. No descontar stock aún.");
                break;
            case "DESPACHO":
                // Aquí llamarás a tu InventarioService para descontar la cerveza
                System.out.println("Descontando stock real para el evento: " + actividad.getEventoId());
                break;
            case "CIERRE_CUENTA":
                // Aquí llamarás a VentaRepository para formalizar el dinero
                System.out.println("Cerrando cuenta y registrando ingreso de dinero");
                break;
            default:
                System.out.println("Evento genérico registrado: " + actividad.getTipoEvento());
                break;
        }
    }
}