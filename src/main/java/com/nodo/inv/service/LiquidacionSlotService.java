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
        // Regla de Oro: Validar que no tenga uno vigente
        Optional<AcuerdoPagoSlot> vigente = acuerdoRepo.findByUsuarioSlotIdAndEstado(acuerdo.getUsuarioSlot().getId(), "VIGENTE");
        if (vigente.isPresent()) {
            throw new RuntimeException("El operario ya tiene un contrato VIGENTE. Debe finalizarlo antes de crear uno nuevo.");
        }

        // Autogenerar Radicado y Fechas
        acuerdo.setRadicado("CNT-" + System.currentTimeMillis());
        acuerdo.setEstado("VIGENTE");
        acuerdo.setFechaCreacion(LocalDateTime.now());
        
        return acuerdoRepo.save(acuerdo);
    }
    
    @Transactional
    public void finalizarAcuerdo(Long acuerdoId) {
        AcuerdoPagoSlot acuerdo = acuerdoRepo.findById(acuerdoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
                
        acuerdo.setEstado("FINALIZADO");
        acuerdo.setFechaFin(LocalDateTime.now());
        acuerdoRepo.save(acuerdo);
    }
    
    @Transactional(readOnly = true)
    public AcuerdoPagoSlot obtenerAcuerdoActivo(Long slotId) {
        return acuerdoRepo.findByUsuarioSlotIdAndEstado(slotId, "VIGENTE").orElse(null);
    }
    
    // ========================================================================
    // 2. GESTIÓN DE NOVEDADES Y VENTAS PREVIAS
    // ========================================================================
    
    @Transactional
    public NovedadSlot registrarNovedad(NovedadSlot novedad) {
        novedad.setFechaRegistro(LocalDateTime.now());
        novedad.setAplicada(false); // Siempre nace como pendiente de cobrar/pagar
        return novedadRepo.save(novedad);
    }

    @Transactional(readOnly = true)
    public List<Venta> consultarVentasRango(Long slotId, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicioDia = fechaInicio.atStartOfDay();
        LocalDateTime finDia = fechaFin.atTime(LocalTime.MAX);
        // Busca en la base de datos el dinero real recaudado en inv_venta
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
        BigDecimal baseVentas = ventas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. DÍAS TRABAJADOS (Días únicos con ventas)
        long diasTrabajados = ventas.stream().map(v -> v.getFecha().toLocalDate()).distinct().count();

        // 3. NOVEDADES
        List<NovedadSlot> novedades = novedadRepo.findByUsuarioSlotIdAndAplicadaFalseAndFechaRegistroBefore(slotId, finDia);
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

        // Quemar novedades (Auditoría)
        LocalDateTime finDia = fechaFin.atTime(LocalTime.MAX);
        List<NovedadSlot> novedades = novedadRepo.findByUsuarioSlotIdAndAplicadaFalseAndFechaRegistroBefore(slotId, finDia);
        
        for (NovedadSlot nov : novedades) {
            nov.setAplicada(true);
            nov.setLiquidacionSlot(guardada);
        }
        novedadRepo.saveAll(novedades);
        
        return guardada;
    }
}