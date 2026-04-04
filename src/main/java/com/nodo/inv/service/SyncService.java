package com.nodo.inv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.EventoOperativoDTO;
import com.nodo.inv.dto.ReporteEstadisticoDueloDTO;
import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.entity.ActividadOperativa;
import com.nodo.inv.entity.Duelo;
import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.HistoricoDuelo;
import com.nodo.inv.entity.Mesa;
import com.nodo.inv.repository.ActividadOperativaRepository;
import com.nodo.inv.repository.DueloRepository;
import com.nodo.inv.repository.EmpresaRepository;
import com.nodo.inv.repository.HistoricoDueloRepository;
import com.nodo.inv.repository.MesaRepository;
import com.nodo.inv.repository.UsuarioOperativoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            if (data != null && data.containsKey("idMesa")) {
                try {
                    Integer idMesaLocal = ((Number) data.get("idMesa")).intValue();
                    Mesa mesa = mesaRepo.findByEmpresaIdAndIdMesaLocal(empresa.getId(), idMesaLocal)
                            .orElse(new Mesa());
                    if (mesa.getId() == null) {
                        mesa.setEmpresa(empresa);
                        mesa.setIdMesaLocal(idMesaLocal);
                        mesa.setNombre("Mesa " + idMesaLocal);
                        mesa = mesaRepo.save(mesa);
                    }
                    actividad.setMesa(mesa);
                } catch (Exception e) {}
            }

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

            Map<String, Object> broadcastPayload = new HashMap<>();
            broadcastPayload.put("tipo", evento.getTipoEvento());
            broadcastPayload.put("data", data);
            broadcastPayload.put("fecha", evento.getFechaDispositivo());
            broadcastPayload.put("terminalUuid", paquete.getTerminalUuid());
            messagingTemplate.convertAndSend(topicMonitor, broadcastPayload);

            ejecutarLogicaDeNegocio(actividad, data);
        }

        // 🔥 CORRECCIÓN CRÍTICA DE WEBSOCKET: Ahora las ventas de ListaClientes y Duelo se reflejarán en React
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
                prepararMesa(actividad, data);
                break;
            case "DUELO_INICIADO":
                iniciarDuelo(actividad, data);
                break;
            case "MESA_CERRADA":
                liberarMesa(actividad, data); // (Clic sostenido)
                break;
            case "DUELO_FINALIZADO_ESTADISTICO":
                procesarYGuardarEstadisticas(actividad, data);
                finalizarDueloYMantenerMesa(actividad, data); // 🔥 NUEVA REGLA (Mantiene la mesa viva)
                break;
        }
    }

    // =========================================================================
    // LÓGICA DE ESTADOS
    // =========================================================================

    private void registrarMesaFisica(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        Mesa mesa = actividad.getMesa();
        mesa.setEstado("DISPONIBLE");
        mesa.setTarifaTiempo(null);
        mesa.setFechaApertura(null);
        mesa.setFechaCierre(null);
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
        if (data.containsKey("idUsuarioSlot")) {
            Long idSlot = ((Number) data.get("idUsuarioSlot")).longValue();
            usuarioOperativoRepo.findById(idSlot).ifPresent(mesa::setUsuarioActual);
        }
        mesaRepo.save(mesa);
        notificarMonitorWeb(mesa, data);
    }

    // 🔥 NUEVA FUNCIÓN: Termina el juego pero deja la mesa configurada ("En espera")
    private void finalizarDueloYMantenerMesa(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        Mesa mesa = actividad.getMesa();
        
        mesa.setEstado("ABIERTO"); // Regresa a Naranja
        mesa.setFechaApertura(null); // Detenemos el cronómetro
        // NO BORRAMOS el usuarioActual ni el tipoJuego. La mesa sigue pre-configurada.
        mesaRepo.save(mesa);

        // Solo cerramos el duelo
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
        mesa.setEstado("DISPONIBLE"); // Se apaga la mesa por completo
        mesa.setFechaCierre(actividad.getFechaDispositivo());
        mesa.setUsuarioActual(null);
        mesa.setTarifaTiempo(null);
        mesa.setFechaApertura(null);
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

        if (data != null && data.containsKey("tipoJuego")) {
            statusPayload.put("tipoJuego", data.get("tipoJuego").toString());
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