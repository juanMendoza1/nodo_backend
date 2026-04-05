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
            
            // 🛡️ 1. Filtro de Duplicados (Lote actual y Base de Datos)
            if (eventosProcesadosEnEsteLote.contains(evento.getEventoId()) || 
                actividadRepo.existsByEventoId(evento.getEventoId())) {
                omitidos++;
                continue;
            }
            eventosProcesadosEnEsteLote.add(evento.getEventoId());

            // 📝 2. Preparar la entidad Actividad
            ActividadOperativa actividad = new ActividadOperativa();
            actividad.setEventoId(evento.getEventoId());
            actividad.setTerminalUuid(paquete.getTerminalUuid());
            actividad.setTipoEvento(evento.getTipoEvento());
            actividad.setFechaDispositivo(evento.getFechaDispositivo());
            actividad.setFechaServidor(LocalDateTime.now());
            actividad.setEmpresa(empresa);
            actividad.setEstadoProcesamiento("PROCESADO");

            Map<String, Object> data = evento.getData();
            if (data == null) data = new HashMap<>(); 

            // 🔥 3. VÍNCULO FÍSICO Y CREACIÓN ON-DEMAND (SOLUCIÓN AL N/A)
            if (data.containsKey("idMesa")) {
                try {
                    Integer idMesaLocal = ((Number) data.get("idMesa")).intValue();
                    
                    // Buscamos si la mesa ya existe en la base de datos del servidor
                    Optional<Mesa> mesaOpt = mesaRepo.findByEmpresaIdAndIdMesaLocal(empresa.getId(), idMesaLocal);
                    
                    Mesa mesaEntity;
                    if (mesaOpt.isPresent()) {
                        mesaEntity = mesaOpt.get();
                    } else if ("MESA_CREADA".equals(evento.getTipoEvento())) {
                        // Si el evento es creación y no existe en pos_mesa, la insertamos ahora
                        mesaEntity = new Mesa();
                        mesaEntity.setEmpresa(empresa);
                        mesaEntity.setIdMesaLocal(idMesaLocal);
                        mesaEntity.setNombre("Mesa " + idMesaLocal);
                        mesaEntity.setEstado("DISPONIBLE");
                        mesaEntity = mesaRepo.save(mesaEntity); 
                        System.out.println("Mesa " + idMesaLocal + " creada físicamente en base de datos.");
                    } else {
                        mesaEntity = null;
                    }

                    if (mesaEntity != null) {
                        actividad.setMesa(mesaEntity); // Se vincula para la columna mes_ideregistro
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando vinculación de mesa: " + e.getMessage());
                }
            }

            // 4. Gestión de Duelo (Si el evento incluye UUID)
            if (data.containsKey("uuidDuelo")) {
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

            // 5. Serializar detalles a JSON
            try {
                actividad.setDetallesJson(objectMapper.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                actividad.setDetallesJson("{}");
            }

            // 🛡️ 6. GUARDADO DE ACTIVIDAD (Con mesa ya vinculada)
            actividad = actividadRepo.save(actividad); 
            procesados++;

            // ⚡ 7. LÓGICA DE NEGOCIO (Actualiza estados de Mesa/Duelo en BD)
            ejecutarLogicaDeNegocio(actividad, data);

            // 📡 8. NOTIFICACIÓN MONITOR OPERATIVO (WebSockets)
            Map<String, Object> broadcastPayload = new HashMap<>();
            broadcastPayload.put("tipo", evento.getTipoEvento());
            broadcastPayload.put("data", data);
            broadcastPayload.put("fecha", evento.getFechaDispositivo());
            broadcastPayload.put("terminalUuid", paquete.getTerminalUuid());
            messagingTemplate.convertAndSend(topicMonitor, broadcastPayload);
        }

        // 🔥 9. REFRESH DASHBOARD: Notifica a React que hay nuevos datos procesados
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
            case "CLIENTE_NUEVO": // Soporte universal para Restaurante/Bar
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
        
        // 🔥 PERSISTENCIA: La mesa memoriza el juego para evitar "N/A" al recargar
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
        
        // 🔥 PERSISTENCIA: Sincroniza el juego actual en la mesa
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

    private void finalizarDueloYMantenerMesa(ActividadOperativa actividad, Map<String, Object> data) {
        if (actividad.getMesa() == null) return;
        Mesa mesa = actividad.getMesa();
        mesa.setEstado("ABIERTO"); // Regresa a Naranja (En espera de otro cliente o juego)
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
        mesa.setEstado("DISPONIBLE"); // Se apaga la mesa por completo (Click Sostenido Tablet)
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

        // 🔥 CORRECCIÓN: Leemos el juego directamente desde la entidad persistida
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