package com.nodo.inv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.inv.dto.EventoOperativoDTO;
import com.nodo.inv.dto.SincronizacionPaqueteDTO;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import com.nodo.inv.service.strategy.EventoOperativoStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncService {

    private final ActividadOperativaRepository actividadRepo;
    private final EmpresaRepository empresaRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final MesaRepository mesaRepo;
    private final UsuarioOperativoRepository usuarioOperativoRepo;
    private final DueloRepository dueloRepo;
    private final PedidoRepository pedidoRepo;
    private final TerminalDispositivoRepository terminalRepo;

    // 🔥 EL MAPA DE ESTRATEGIAS (Patrón Strategy en O(1))
    private final Map<String, EventoOperativoStrategy> estrategias;

    @Autowired
    public SyncService(
            ActividadOperativaRepository actividadRepo,
            EmpresaRepository empresaRepo,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper,
            MesaRepository mesaRepo,
            UsuarioOperativoRepository usuarioOperativoRepo,
            DueloRepository dueloRepo,
            PedidoRepository pedidoRepo,
            TerminalDispositivoRepository terminalRepo,
            // 🚀 MAGIA: Spring inyecta automáticamente todas las clases que implementen esta interfaz
            List<EventoOperativoStrategy> strategyList) {
        
        this.actividadRepo = actividadRepo;
        this.empresaRepo = empresaRepo;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.mesaRepo = mesaRepo;
        this.usuarioOperativoRepo = usuarioOperativoRepo;
        this.dueloRepo = dueloRepo;
        this.pedidoRepo = pedidoRepo;
        this.terminalRepo = terminalRepo;
        
        // Convertimos la lista a un Diccionario (Map) usando el tipo de evento como llave
        this.estrategias = strategyList.stream()
                .collect(Collectors.toMap(EventoOperativoStrategy::getTipoEvento, s -> s));
    }

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

            // 🔥 3. VÍNCULO FÍSICO Y CREACIÓN ON-DEMAND
            if (data.containsKey("idMesa")) {
                try {
                    Integer idMesaLocal = ((Number) data.get("idMesa")).intValue();
                    Optional<Mesa> mesaOpt = mesaRepo.findByEmpresaIdAndIdMesaLocal(empresa.getId(), idMesaLocal);
                    
                    Mesa mesaEntity;
                    if (mesaOpt.isPresent()) {
                        mesaEntity = mesaOpt.get();
                    } else if ("MESA_CREADA".equals(evento.getTipoEvento())) {
                        mesaEntity = new Mesa();
                        mesaEntity.setEmpresa(empresa);
                        mesaEntity.setIdMesaLocal(idMesaLocal);
                        mesaEntity.setNombre("Mesa " + idMesaLocal);
                        mesaEntity.setEstado("DISPONIBLE");
                        mesaEntity = mesaRepo.save(mesaEntity); 
                    } else {
                        mesaEntity = null;
                    }

                    if (mesaEntity != null) {
                        actividad.setMesa(mesaEntity);
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

            // 🛡️ 6. GUARDADO DE ACTIVIDAD
            actividad = actividadRepo.save(actividad); 
            procesados++;

            // ⚡ 7. LÓGICA DE NEGOCIO (Patrón Strategy + Comanda)
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
        
        // 1. BUSCAMOS Y EJECUTAMOS LA ESTRATEGIA (Si existe una clase para este evento)
        EventoOperativoStrategy estrategia = estrategias.get(actividad.getTipoEvento());
        if (estrategia != null) {
            estrategia.procesar(actividad, data);
        }

        // 2. GESTIÓN TRANSACCIONAL DEL PEDIDO (COMANDA)
        // La dejamos aquí porque es transversal a múltiples eventos (apertura, despacho, cierre)
        gestionarComandaTransaccional(actividad, data);
        
        // 3. NOTIFICACIÓN WEBSOCKET
        if (actividad.getMesa() != null) {
            notificarMonitorWeb(actividad.getMesa(), data);
        }
    }

    private void notificarMonitorWeb(Mesa mesa, Map<String, Object> data) {
        Map<String, Object> statusPayload = new HashMap<>();
        statusPayload.put("idMesaLocal", mesa.getIdMesaLocal());
        statusPayload.put("estado", mesa.getEstado());
        statusPayload.put("fechaApertura", mesa.getFechaApertura());
        statusPayload.put("tarifaTiempo", mesa.getTarifaTiempo());

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
    
    private void gestionarComandaTransaccional(ActividadOperativa actividad, Map<String, Object> data) {
        String tipoEvento = actividad.getTipoEvento();
        Mesa mesa = actividad.getMesa();
        Empresa empresa = actividad.getEmpresa();

        if (mesa == null) return; 

        LocalDateTime fechaTransaccion = Instant.ofEpochMilli(actividad.getFechaDispositivo())
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime();

        TerminalDispositivo terminal = null;
        Programa programa = null; 
        
        if (actividad.getTerminalUuid() != null) {
            terminal = terminalRepo.findByUuidHardware(actividad.getTerminalUuid()).orElse(null);
            if (terminal != null) {
                programa = terminal.getPrograma();
            }
        }

        UsuarioOperativo operario = null;
        if (data.containsKey("idUsuarioSlot")) {
            Long idSlot = ((Number) data.get("idUsuarioSlot")).longValue();
            operario = usuarioOperativoRepo.findById(idSlot).orElse(null);
            if (programa == null && operario != null && operario.getPrograma() != null) {
                programa = operario.getPrograma();
            }
        }

        // 4. APERTURA DE LA CUENTA
        if (List.of("MESA_ABIERTA", "DUELO_INICIADO").contains(tipoEvento)) {
            Pedido pedido = pedidoRepo.findByEmpresaAndMesaAndEstado(empresa, mesa, "ABIERTA")
                    .orElse(new Pedido());

            if (pedido.getIdPedido() == null) {
                pedido.setEmpresa(empresa);
                pedido.setMesa(mesa);
                pedido.setEstado("ABIERTA");
                pedido.setFechaApertura(fechaTransaccion); 
                pedido.setPrograma(programa);
                pedido.setTerminal(terminal);
                pedido.setOperario(operario);
                pedido.setTipoComanda(tipoEvento); 
                pedido.setTotalCalculado(BigDecimal.ZERO);
                pedidoRepo.save(pedido);
            }
        }

        // 5. AGREGAR PRODUCTOS AL CARRITO 
        if (List.of("DESPACHO_MESA", "PEDIDO_DIRECTO", "DESPACHO").contains(tipoEvento)) {
            Pedido pedido = pedidoRepo.findByEmpresaAndMesaAndEstado(empresa, mesa, "ABIERTA").orElse(null);
            if (pedido == null) {
                pedido = new Pedido();
                pedido.setEmpresa(empresa);
                pedido.setMesa(mesa);
                pedido.setEstado("ABIERTA");
                pedido.setFechaApertura(fechaTransaccion);
                pedido.setPrograma(programa);
                pedido.setTerminal(terminal);
                pedido.setOperario(operario);
                pedido.setTipoComanda(tipoEvento);
                pedido.setTotalCalculado(BigDecimal.ZERO);
                pedido = pedidoRepo.save(pedido); 
            }
            
            String cliente = (String) data.getOrDefault("clienteNombre", 
                    data.getOrDefault("nombreCliente", data.getOrDefault("nombreJugador", "Desconocido")));

            List<Map<String, Object>> productos = (List<Map<String, Object>>) data.get("productos");
            if (productos != null) {
                for (Map<String, Object> p : productos) {
                    agregarItemAlPedido(pedido, p, cliente, fechaTransaccion);
                }
            } else if (data.containsKey("nombre") && data.containsKey("precio")) {
                agregarItemAlPedido(pedido, data, cliente, fechaTransaccion);
            }
            
            pedidoRepo.save(pedido); 
        }

        // 6. CIERRE DE LA CUENTA
        if ("MESA_CERRADA".equals(tipoEvento)) {
            Pedido pedido = pedidoRepo.findByEmpresaAndMesaAndEstado(empresa, mesa, "ABIERTA").orElse(null);
            if (pedido != null) {
                pedido.setEstado("CERRADA");
                pedido.setFechaCierre(fechaTransaccion);
                
                BigDecimal total = pedido.getDetalles().stream()
                        .map(PedidoDetalle::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                pedido.setTotalCalculado(total);
                pedidoRepo.save(pedido);
            }
        }
    }

    private void agregarItemAlPedido(Pedido pedido, Map<String, Object> itemData, String cliente, LocalDateTime fecha) {
        String nombre = (String) itemData.getOrDefault("nombre", "Item Desconocido");
        Integer cant = itemData.containsKey("cantidad") ? Integer.parseInt(itemData.get("cantidad").toString()) : 1;
        BigDecimal precio = itemData.containsKey("precio") ? new BigDecimal(itemData.get("precio").toString()) : BigDecimal.ZERO;

        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setPedido(pedido);
        detalle.setNombreItem(nombre);
        detalle.setCantidad(cant);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(precio.multiply(new BigDecimal(cant)));
        detalle.setTipoItem("PRODUCTO_FISICO");
        detalle.setDueñoEspecifico(cliente);
        detalle.setFechaAgregado(fecha);

        if (pedido.getDetalles() == null) {
            pedido.setDetalles(new ArrayList<>());
        }
        pedido.getDetalles().add(detalle);
    }
}