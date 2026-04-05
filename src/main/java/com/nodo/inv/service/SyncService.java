package com.nodo.inv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.EventoOperativoDTO;
import com.nodo.inv.dto.ReporteEstadisticoDueloDTO;
import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private UsuarioOperativoRepository usuarioOperativoRepo;
    @Autowired
    private DueloRepository dueloRepo;

    @Transactional
    public Map<String, Object> procesarPaquete(SincronizacionPaqueteDTO paquete) {
        int procesados = 0;
        int omitidos = 0;

        Empresa empresa = empresaRepo.findById(paquete.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        String topicMonitor = "/topic/monitor-operativo/" + paquete.getEmpresaId();
        Set<String> eventosProcesadosEnEsteLote = new HashSet<>();

        for (EventoOperativoDTO evento : paquete.getEventos()) {
            
            // 🛡️ Filtro de Duplicados en el mismo lote o ya existentes en BD
            if (eventosProcesadosEnEsteLote.contains(evento.getEventoId()) || 
                actividadRepo.existsByEventoId(evento.getEventoId())) {
                omitidos++;
                continue;
            }
            eventosProcesadosEnEsteLote.add(evento.getEventoId());

            ActividadOperativa actividad = new ActividadOperativa();
            actividad.setEventoId(evento.getEventoId());
            actividad.setTerminalUuid(paquete.getTerminalUuid());
            actividad.setTipoEvento(evento.getTipoEvento());
            actividad.setFechaDispositivo(evento.getFechaDispositivo());
            actividad.setFechaServidor(LocalDateTime.now());
            actividad.setEmpresa(empresa);
            actividad.setEstadoProcesamiento("PROCESADO");

            Map<String, Object> data = evento.getData();

            // 🔥 CORRECCIÓN: VINCULAR MESA PARA EVITAR "UBICACIÓN N/A"
            if (data != null && data.containsKey("idMesa")) {
                try {
                    Integer idMesaLocal = ((Number) data.get("idMesa")).intValue();
                    mesaRepo.findByEmpresaIdAndIdMesaLocal(empresa.getId(), idMesaLocal)
                            .ifPresent(actividad::setMesa);
                } catch (Exception e) {
                    System.err.println("Error vinculando mesa a actividad: " + e.getMessage());
                }
            }

            // Gestión de Duelo
            if (data != null && data.containsKey("uuidDuelo")) {
                try {
                    String uuid = data.get("uuidDuelo").toString();
                    Duelo duelo = dueloRepo.findByUuidDuelo(uuid).orElse(new Duelo());
                    if (duelo.getId() == null) {
                        duelo.setUuidDuelo(uuid);
                        duelo.setEmpresa(empresa);
                        if (actividad.getMesa() != null) duelo.setMesa(actividad.getMesa());
                        duelo = dueloRepo.save(duelo);
                    }
                    actividad.setDuelo(duelo);
                } catch (Exception e) {}
            }

            try {
                actividad.setDetallesJson(objectMapper.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                actividad.setDetallesJson("{}");
            }

            actividad = actividadRepo.save(actividad);
            procesados++;

            // Notificación vía WebSocket para Monitor Operativo (crudo)
            Map<String, Object> broadcastPayload = new HashMap<>();
            broadcastPayload.put("tipo", evento.getTipoEvento());
            broadcastPayload.put("data", data);
            broadcastPayload.put("fecha", evento.getFechaDispositivo());
            broadcastPayload.put("terminalUuid", paquete.getTerminalUuid());
            messagingTemplate.convertAndSend(topicMonitor, broadcastPayload);

            // Ejecución de cambios de estado en la base de datos
            ejecutarLogicaDeNegocio(actividad, data);
        }

        // 🔥 GATILLO REACT: Despierta el Dashboard y los paneles de mesa
        if (procesados > 0) {
            messagingTemplate.convertAndSend("/topic/empresa/" + paquete.getEmpresaId() + "/dashboard", "NUEVA_VENTA");
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("procesados", procesados);
        respuesta.put("omitidos", omitidos);

        return respuesta;
    }

    private void ejecutarLogicaDeNegocio(ActividadOperativa actividad, Map<String, Object> data) {
        switch (actividad.getTipoEvento()) {
            case "MESA_CREADA":
                registrarMesaFisica(actividad, data);
                break;
            case "MESA_ABIERTA":
            case "CLIENTE_NUEVO": // Soporte universal para restaurante/bar
                prepararMesa(actividad, data);
                break;
            case "DUELO_INICIADO":
                iniciarDuelo(actividad, data);
                break;
            case "MESA_CERRADA":
                liberarMesa(actividad, data);
                break;
            case "DUELO_FINALIZADO_ESTADISTICO":
                procesarYGuardarEstadisticas(actividad, data);
                finalizarDueloYMantenerMesa(actividad, data);
                break;
        }
    }

    // =========================================================================
    // LÓGICA DE ESTADOS PERSISTENTES
    // =========================================================================

    private void registrarMesaFisica(ActividadOperativa actividad, Map<String, Object> data) {
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
        notificarMonitorWeb(mesa, data);
    }

    private void prepararMesa(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        Mesa mesa = actividad.getMesa();
        mesa.setEstado("ABIERTO");
        
        // 🔥 PERSISTENCIA: Guardamos el tipo de juego para evitar N/A al refrescar
        if (data != null && data.containsKey("tipoJuego")) {
            mesa.setTipoJuego(data.get("tipoJuego").toString());
        }
        
        if (data != null && data.containsKey("idUsuarioSlot")) {
            Long idSlot = ((Number) data.get("idUsuarioSlot")).longValue();
            usuarioOperativoRepo.findById(idSlot).ifPresent(mesa::setUsuarioActual);
        }
        mesaRepo.save(mesa);
        notificarMonitorWeb(mesa, data);
    }

    private void iniciarDuelo(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;

        if (actividad.getDuelo() != null) {
            Duelo duelo = actividad.getDuelo();
            duelo.setEstado("EN_CURSO");
            duelo.setFechaInicio(actividad.getFechaDispositivo());
            if (data.containsKey("tipoJuego")) duelo.setTipoJuego(data.get("tipoJuego").toString());
            if (data.containsKey("reglaDuelo")) duelo.setReglaDuelo(data.get("reglaDuelo").toString());
            if (data.containsKey("tarifaTiempo") && data.get("tarifaTiempo") != null) {
                duelo.setTarifaTiempo(new BigDecimal(data.get("tarifaTiempo").toString()));
            }
            if (data.containsKey("idUsuarioSlot")) {
                Long idSlot = ((Number) data.get("idUsuarioSlot")).longValue();
                usuarioOperativoRepo.findById(idSlot).ifPresent(duelo::setUsuarioOperativo);
            }
            dueloRepo.save(duelo);
        }

        Mesa mesa = actividad.getMesa();
        mesa.setEstado("OCUPADA");
        mesa.setFechaApertura(actividad.getFechaDispositivo());
        
        // 🔥 PERSISTENCIA: Se asegura de que la mesa recuerde el juego
        if (data != null && data.containsKey("tipoJuego")) {
            mesa.setTipoJuego(data.get("tipoJuego").toString());
        }
        
        if (data.containsKey("idUsuarioSlot")) {
            Long idSlot = ((Number) data.get("idUsuarioSlot")).longValue();
            usuarioOperativoRepo.findById(idSlot).ifPresent(mesa::setUsuarioActual);
        }
        mesaRepo.save(mesa);
        notificarMonitorWeb(mesa, data);
    }

    private void finalizarDueloYMantenerMesa(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        Mesa mesa = actividad.getMesa();
        mesa.setEstado("ABIERTO"); // Regresa a Naranja (En espera)
        mesa.setFechaApertura(null); 
        mesaRepo.save(mesa);

        dueloRepo.findByMesaAndEstado(mesa, "EN_CURSO").ifPresent(dueloActivo -> {
            dueloActivo.setEstado("FINALIZADO");
            dueloActivo.setFechaFin(actividad.getFechaDispositivo());
            dueloRepo.save(dueloActivo);
        });

        notificarMonitorWeb(mesa, data);
    }

    private void liberarMesa(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        Mesa mesa = actividad.getMesa();
        mesa.setEstado("DISPONIBLE"); 
        mesa.setFechaCierre(actividad.getFechaDispositivo());
        mesa.setUsuarioActual(null);
        mesa.setTarifaTiempo(null);
        mesa.setFechaApertura(null);
        mesa.setTipoJuego(null);
        mesaRepo.save(mesa);

        dueloRepo.findByMesaAndEstado(mesa, "EN_CURSO").ifPresent(dueloActivo -> {
            dueloActivo.setEstado("FINALIZADO");
            dueloActivo.setFechaFin(actividad.getFechaDispositivo());
            dueloRepo.save(dueloActivo);
        });

        notificarMonitorWeb(mesa, data);
    }

    private void procesarYGuardarEstadisticas(ActividadOperativa actividad, Map<String, Object> data) {
        try {
            ReporteEstadisticoDueloDTO reporteDto = objectMapper.convertValue(data, ReporteEstadisticoDueloDTO.class);
            if (historicoRepository.existsByUuidDuelo(reporteDto.getUuidDuelo())) return;
            HistoricoDuelo historico = new HistoricoDuelo();
            historico.setUuidDuelo(reporteDto.getUuidDuelo());
            historico.setIdMesa(reporteDto.getIdMesa());
            historico.setTipoJuego(reporteDto.getTipoJuego());
            historico.setFechaFinalizacion(LocalDateTime.now());
            historico.setEmpresa(actividad.getEmpresa());
            historico.setDetalleJson(objectMapper.writeValueAsString(reporteDto));
            historicoRepository.save(historico);
            messagingTemplate.convertAndSend("/topic/duelos/" + actividad.getEmpresa().getId(), reporteDto);
        } catch (Exception e) {}
    }

    private void notificarMonitorWeb(Mesa mesa, Map<String, Object> data) {
        Map<String, Object> statusPayload = new HashMap<>();
        statusPayload.put("idMesaLocal", mesa.getIdMesaLocal());
        statusPayload.put("estado", mesa.getEstado());
        statusPayload.put("fechaApertura", mesa.getFechaApertura());
        statusPayload.put("tarifaTiempo", mesa.getTarifaTiempo());

        // 🔥 OBLIGATORIO: Tomar el tipo de juego SIEMPRE de la entidad Mesa (BD)
        if (mesa.getTipoJuego() != null) {
            statusPayload.put("tipoJuego", mesa.getTipoJuego());
        }

        if (mesa.getUsuarioActual() != null) {
            Map<String, String> user = new HashMap<>();
            user.put("alias", mesa.getUsuarioActual().getAlias());
            user.put("login", mesa.getUsuarioActual().getLogin());
            statusPayload.put("usuarioActual", user);
        } else {
            statusPayload.put("usuarioActual", null);
        }
        messagingTemplate.convertAndSend("/topic/mesas/" + mesa.getEmpresa().getId(), statusPayload);
    }
}