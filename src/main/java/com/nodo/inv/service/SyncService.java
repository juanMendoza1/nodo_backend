package com.nodo.inv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.EventoOperativoDTO;
import com.nodo.inv.dto.ReporteEstadisticoDueloDTO;
import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.entity.ActividadOperativa;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.HistoricoDuelo;
import com.nodo.inv.entity.Mesa;
import com.nodo.inv.repository.ActividadOperativaRepository;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.HistoricoDueloRepository;
import com.nodo.inv.repository.MesaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SyncService {

    @Autowired
    private ActividadOperativaRepository actividadRepo;

    @Autowired
    private EmpresaRepository empresaRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HistoricoDueloRepository historicoRepository;
    
    @Autowired
    private MesaRepository mesaRepo;

    @Transactional
    public Map<String, Object> procesarPaquete(SincronizacionPaqueteDTO paquete) {
        int procesados = 0;
        int omitidos = 0;

        Empresa empresa = empresaRepo.findById(paquete.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        String topicMonitor = "/topic/monitor-operativo/" + paquete.getEmpresaId();

        for (EventoOperativoDTO evento : paquete.getEventos()) {
            if (actividadRepo.existsByEventoId(evento.getEventoId())) {
                omitidos++;
                continue;
            }

            ActividadOperativa actividad = new ActividadOperativa();
            actividad.setEventoId(evento.getEventoId());
            actividad.setTerminalUuid(paquete.getTerminalUuid());
            actividad.setTipoEvento(evento.getTipoEvento());
            actividad.setFechaDispositivo(evento.getFechaDispositivo());
            actividad.setFechaServidor(LocalDateTime.now());
            actividad.setEmpresa(empresa);
            actividad.setEstadoProcesamiento("PROCESADO");

            try {
                actividad.setDetallesJson(objectMapper.writeValueAsString(evento.getData()));
            } catch (JsonProcessingException e) {
                actividad.setDetallesJson("{}");
            }

            actividadRepo.save(actividad);
            procesados++;

            Map<String, Object> broadcastPayload = new HashMap<>();
            broadcastPayload.put("tipo", evento.getTipoEvento());
            broadcastPayload.put("data", evento.getData());
            broadcastPayload.put("fecha", evento.getFechaDispositivo());
            broadcastPayload.put("terminalUuid", paquete.getTerminalUuid());
            messagingTemplate.convertAndSend(topicMonitor, broadcastPayload);

            ejecutarLogicaDeNegocio(actividad, evento.getData());
        }

        if (procesados > 0) {
            messagingTemplate.convertAndSend("/topic/admin/updates/" + paquete.getEmpresaId(), "NUEVA_ACTIVIDAD");
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("procesados", procesados);
        respuesta.put("omitidos", omitidos);

        return respuesta;
    }

    private void ejecutarLogicaDeNegocio(ActividadOperativa actividad, Map<String, Object> data) {
        switch (actividad.getTipoEvento()) {
        case "MESA_ABIERTA":
            actualizarEstadoMesa(actividad, data, "ABIERTO");
            break;
        case "MESA_CERRADA":
            actualizarEstadoMesa(actividad, data, "CERRADO");
            break;
        case "DUELO_FINALIZADO_ESTADISTICO":
            procesarYGuardarEstadisticas(actividad, data);
            break;
            case "PEDIDO_NUEVO":
            case "PEDIDO_DIRECTO":
            case "DESPACHO":
            case "CIERRE_CUENTA":
                break;
        }
    }

    private void procesarYGuardarEstadisticas(ActividadOperativa actividad, Map<String, Object> data) {
        try {
            ReporteEstadisticoDueloDTO reporteDto = objectMapper.convertValue(data, ReporteEstadisticoDueloDTO.class);

            if (historicoRepository.existsByUuidDuelo(reporteDto.getUuidDuelo())) {
                return;
            }

            HistoricoDuelo historico = new HistoricoDuelo();
            historico.setUuidDuelo(reporteDto.getUuidDuelo());
            historico.setIdMesa(reporteDto.getIdMesa());
            historico.setTipoJuego(reporteDto.getTipoJuego());
            historico.setFechaFinalizacion(LocalDateTime.now());
            historico.setEmpresa(actividad.getEmpresa());
            historico.setDetalleJson(objectMapper.writeValueAsString(reporteDto));

            historicoRepository.save(historico);

            messagingTemplate.convertAndSend("/topic/duelos/" + actividad.getEmpresa().getId(), reporteDto);

        } catch (Exception e) {
        }
    }
    
    private void actualizarEstadoMesa(ActividadOperativa actividad, Map<String, Object> data, String nuevoEstado) {
        try {
            // Extraemos el ID local de la mesa desde el JSON de detalles
            Integer idMesaLocal = (Integer) data.get("idMesa");
            
            // Buscamos si la mesa ya existe para esta empresa
            Mesa mesa = mesaRepo.findByEmpresaIdAndIdMesaLocal(actividad.getEmpresa().getId(), idMesaLocal)
                    .orElse(new Mesa());

            // Si es nueva, configuramos los datos básicos
            if (mesa.getId() == null) {
                mesa.setEmpresa(actividad.getEmpresa());
                mesa.setIdMesaLocal(idMesaLocal);
                mesa.setNombre("Mesa " + idMesaLocal);
            }

            // Actualizamos según el evento
            mesa.setEstado(nuevoEstado);
            
            if ("ABIERTO".equals(nuevoEstado)) {
                mesa.setFechaApertura(actividad.getFechaDispositivo());
                mesa.setTipoJuego((String) data.get("tipoJuego"));
                mesa.setTarifaTiempo(new BigDecimal(data.get("tarifaTiempo").toString()));
                mesa.setReglaDuelo((String) data.get("reglaDuelo"));
            } else {
                mesa.setFechaCierre(actividad.getFechaDispositivo());
            }

            mesaRepo.save(mesa);

            // Notificamos por WebSocket para que el monitor web cambie de color
            Map<String, Object> statusPayload = new HashMap<>();
            statusPayload.put("idMesaLocal", idMesaLocal);
            statusPayload.put("estado", nuevoEstado);
            messagingTemplate.convertAndSend("/topic/mesas/" + actividad.getEmpresa().getId(), statusPayload);

        } catch (Exception e) {
            // Log de error si el JSON no tiene el formato esperado
        }
    }
}