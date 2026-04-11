package com.nodo.inv.service;

import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LiquidacionSlotService {

    private final LiquidacionSlotRepository liquidacionRepo;
    private final AcuerdoPagoSlotRepository acuerdoRepo;
    private final NovedadSlotRepository novedadRepo;
    private final VentaRepository ventaRepo; 
    private final UsuarioOperativoRepository usuarioOperativoRepo;

    // ========================================================================
    // 1. GESTIÓN DEL CONTRATO (ACUERDOS DE PAGO)
    // ========================================================================
    
    @Transactional
    public AcuerdoPagoSlot crearAcuerdo(AcuerdoPagoSlot acuerdo) {
        Optional<AcuerdoPagoSlot> vigente = acuerdoRepo.findByUsuarioSlotIdAndEstado(acuerdo.getUsuarioSlot().getId(), "VIGENTE");
        if (vigente.isPresent()) {
            throw new RuntimeException("El operario ya tiene un contrato VIGENTE. Debe finalizarlo antes de crear uno nuevo.");
        }

        acuerdo.setRadicado("CNT-" + System.currentTimeMillis());
        acuerdo.setEstado("VIGENTE");
        acuerdo.setFechaCreacion(LocalDateTime.now());
        
        // 🔥 Regla: Si no envían fecha de inicio, es hoy
        if (acuerdo.getFechaInicio() == null) {
            acuerdo.setFechaInicio(LocalDate.now());
        }
        
        // 🔥 Regla: Si la fecha fin viene vacía, le sumamos 1 año exacto al inicio
        if (acuerdo.getFechaFin() == null) {
            acuerdo.setFechaFin(acuerdo.getFechaInicio().plusYears(1));
        }
        
        return acuerdoRepo.save(acuerdo);
    }
    
    @Transactional
    public AcuerdoPagoSlot actualizarAcuerdo(Long id, AcuerdoPagoSlot datosNuevos) {
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
                
        acuerdo.setTipoAcuerdo(datosNuevos.getTipoAcuerdo());
        acuerdo.setValorFijoDia(datosNuevos.getValorFijoDia());
        acuerdo.setPorcentajeComision(datosNuevos.getPorcentajeComision());
        acuerdo.setFrecuenciaPago(datosNuevos.getFrecuenciaPago());
        acuerdo.setObservaciones(datosNuevos.getObservaciones());
        
        if (datosNuevos.getFechaInicio() != null) {
            acuerdo.setFechaInicio(datosNuevos.getFechaInicio());
        }
        
        if (datosNuevos.getFechaFin() != null) {
            acuerdo.setFechaFin(datosNuevos.getFechaFin());
        } else {
            // Si al editar le borran la fecha fin, recalculamos a 1 año
            acuerdo.setFechaFin(acuerdo.getFechaInicio().plusYears(1));
        }

        return acuerdoRepo.save(acuerdo);
    }
    
    @Transactional
    public void finalizarAcuerdo(Long acuerdoId) {
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findById(acuerdoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
                
        acuerdo.setEstado("FINALIZADO");
        // Actualizamos la fecha de fin al día de hoy porque se cortó el contrato prematuramente
        acuerdo.setFechaFin(LocalDate.now()); 
        acuerdoRepo.save(acuerdo);
    }
    
    @Transactional(readOnly = true)
    public AcuerdoPagoSlot obtenerAcuerdoActivo(Long slotId) {
        return acuerdoRepo.findByUsuarioSlotIdAndEstado(slotId, "VIGENTE").orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<AcuerdoPagoSlot> obtenerHistorialAcuerdos(Long slotId) {
        return acuerdoRepo.findByUsuarioSlotIdAndEstadoNotOrderByFechaCreacionDesc(slotId, "VIGENTE");
    }

    @Transactional
    public void eliminarAcuerdo(Long acuerdoId) {
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findById(acuerdoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
                
        // Borrado físico de la base de datos
        acuerdoRepo.delete(acuerdo);
    }
    
    // ========================================================================
    // 2. GESTIÓN DE NOVEDADES Y VENTAS PREVIAS
    // ========================================================================
    
    @Transactional
    public NovedadSlot registrarNovedad(NovedadSlot novedad, Long acuerdoId) {
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findById(acuerdoId)
            .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
            
        novedad.setAcuerdoPago(acuerdo);
        novedad.setFechaRegistro(LocalDateTime.now());
        novedad.setAplicada(false); 
        return novedadRepo.save(novedad);
    }

    @Transactional(readOnly = true)
    public List<Venta> consultarVentasRango(Long slotId, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicioDia = fechaInicio.atStartOfDay();
        LocalDateTime finDia = fechaFin.atTime(LocalTime.MAX);
        return ventaRepo.findByOperarioAndRangoFechas(slotId, inicioDia, finDia);
    }

    // ========================================================================
    // 3. MOTOR MATEMÁTICO: CÁLCULO Y GENERACIÓN DE PAGOS
    // ========================================================================
    
    @Transactional(readOnly = true)
    public LiquidacionSlot calcularLiquidacion(Long empresaId, Long slotId, LocalDate fechaInicio, LocalDate fechaFin) {
        UsuarioOperativo operario = usuarioOperativoRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Operario no encontrado"));
                
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findByUsuarioSlotIdAndEstado(slotId, "VIGENTE")
                .orElseThrow(() -> new RuntimeException("El operario no tiene un contrato de pago vigente."));

        LocalDateTime inicioDia = fechaInicio.atStartOfDay();
        LocalDateTime finDia = fechaFin.atTime(LocalTime.MAX);

        // 1. BASE DE VENTAS (Usando inv_venta)
        List<Venta> ventas = ventaRepo.findByOperarioAndRangoFechas(slotId, inicioDia, finDia);
        BigDecimal baseVentas = ventas.stream()
                .map(Venta::getGranTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. DÍAS TRABAJADOS (Días únicos con ventas)
        long diasTrabajados = ventas.stream().map(v -> v.getFecha().toLocalDate()).distinct().count();

        // 3. NOVEDADES (Ajustado para usar el contrato)
        List<NovedadSlot> novedades = novedadRepo.findByAcuerdoPagoIdAndAplicadaFalse(acuerdo.getId());
        BigDecimal totalBonos = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;

        for (NovedadSlot nov : novedades) {
            if (List.of("BONO", "PROPINA_EXTRA").contains(nov.getTipoNovedad())) {
                totalBonos = totalBonos.add(nov.getValor());
            } else if (List.of("DESCUENTO", "ANTICIPO").contains(nov.getTipoNovedad())) {
                totalDescuentos = totalDescuentos.add(nov.getValor());
            }
        }

        // 4. FÓRMULA DE PAGO
        BigDecimal totalComision = BigDecimal.ZERO;
        BigDecimal totalFijo = BigDecimal.ZERO;

        if (List.of("COMISION_VENTAS", "MIXTO").contains(acuerdo.getTipoAcuerdo())) {
            BigDecimal porcentaje = acuerdo.getPorcentajeComision().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            totalComision = baseVentas.multiply(porcentaje);
        }

        if (List.of("FIJO_POR_DIA", "MIXTO").contains(acuerdo.getTipoAcuerdo())) {
            totalFijo = acuerdo.getValorFijoDia().multiply(new BigDecimal(diasTrabajados));
        }

        BigDecimal granTotal = totalComision.add(totalFijo).add(totalBonos).subtract(totalDescuentos);

        // 5. BORRADOR
        LiquidacionSlot pre = new LiquidacionSlot();
        pre.setEmpresa(operario.getEmpresa());
        pre.setUsuarioSlot(operario);
        pre.setFechaInicio(fechaInicio);
        pre.setFechaFin(fechaFin);
        pre.setFechaGeneracion(LocalDateTime.now());
        pre.setBaseVentasCalculada(baseVentas);
        pre.setTotalComision(totalComision);
        pre.setTotalFijo(totalFijo);
        pre.setTotalBonos(totalBonos);
        pre.setTotalDescuentos(totalDescuentos);
        pre.setGranTotalPagar(granTotal);
        pre.setEstado("BORRADOR");
        
        return pre;
    }

    @Transactional
    public LiquidacionSlot generarPagoDefinitivo(Long empresaId, Long slotId, LocalDate fechaInicio, LocalDate fechaFin, Usuario admin) {
        // Recalculamos para garantizar precisión
        LiquidacionSlot liquidacion = calcularLiquidacion(empresaId, slotId, fechaInicio, fechaFin);
        liquidacion.setEstado("PAGADA");
        liquidacion.setGeneradaPor(admin);
        
        // Guardar recibo
        LiquidacionSlot guardada = liquidacionRepo.save(liquidacion);

        // 🔥 CORRECCIÓN AQUÍ: Quemar novedades del contrato actual, no genéricas del operario
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findByUsuarioSlotIdAndEstado(slotId, "VIGENTE")
                .orElseThrow(() -> new RuntimeException("El operario no tiene un contrato de pago vigente."));
        
        List<NovedadSlot> novedades = novedadRepo.findByAcuerdoPagoIdAndAplicadaFalse(acuerdo.getId());
        
        for (NovedadSlot nov : novedades) {
            nov.setAplicada(true);
            nov.setLiquidacionSlot(guardada); // Vinculamos la nota al recibo de pago oficial
        }
        novedadRepo.saveAll(novedades);
        
        return guardada;
    }
    
    @Transactional(readOnly = true)
    public List<NovedadSlot> obtenerNovedadesPendientes(Long acuerdoId) {
        return novedadRepo.findByAcuerdoPagoIdAndAplicadaFalse(acuerdoId);
    }
    
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> obtenerHistorialPagosSlot(Long slotId) {
        return liquidacionRepo.findByUsuarioSlotIdOrderByFechaGeneracionDesc(slotId).stream()
            .map(l -> java.util.Map.<String, Object>of(
                "id", l.getId(),
                "fechaInicio", l.getFechaInicio(),
                "fechaFin", l.getFechaFin(),
                "fechaGeneracion", l.getFechaGeneracion(),
                "granTotalPagar", l.getGranTotalPagar(),
                "estado", l.getEstado(),
                "generadaPor", l.getGeneradaPor() != null ? l.getGeneradaPor().getLogin() : "SISTEMA"
            )).toList();
    }
}