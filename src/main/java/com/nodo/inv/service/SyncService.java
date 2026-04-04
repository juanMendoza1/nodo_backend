package com.nodo.inv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.EventoOperativoDTO;
import com.nodo.inv.dto.ReporteEstadisticoDueloDTO;
import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.entity.ActividadOperativa;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.HistoricoDuelo;
import com.nodo.inv.repository.ActividadOperativaRepository;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.HistoricoDueloRepository;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HistoricoDueloRepository historicoRepository;

    @Transactional
    public Map<String, Object> procesarPaquete(SincronizacionPaqueteDTO paquete) {
        int procesados = 0;
        int omitidos = 0;

        Empresa empresa = empresaRepo.findById(paquete.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

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
            // Error silenciado para evitar ralentización
        }
    }
}