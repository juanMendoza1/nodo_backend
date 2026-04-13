package com.nodo.inv.retail.service;

import com.nodo.inv.Utils.EstadoUsuario;
import com.nodo.inv.core.dto.DashboardStatsDTO;
import com.nodo.inv.core.repository.TerminalDispositivoRepository;
import com.nodo.inv.core.repository.UsuarioOperativoRepository;

import com.nodo.inv.retail.dto.MovimientoDTO;
import com.nodo.inv.retail.entity.InventarioMovimiento;
import com.nodo.inv.retail.entity.Producto;
import com.nodo.inv.retail.repository.InventarioMovimientoRepository;
import com.nodo.inv.retail.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioMovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    
    // Inyectamos los demás repositorios para las estadísticas
    private final TerminalDispositivoRepository terminalRepository;
    private final UsuarioOperativoRepository usuarioOperativoRepository;
    
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Procesa un movimiento de inventario y actualiza el stock actual del producto.
     */
    @Transactional
    public InventarioMovimiento registrarMovimiento(InventarioMovimiento mov) {
        Producto producto = productoRepository.findById(mov.getProducto().getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar stock si es una salida (cantidad negativa)
        if (mov.getCantidad() < 0 && producto.getStockActual() < Math.abs(mov.getCantidad())) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        // 1. Actualizar el stock actual en la entidad Producto
        producto.setStockActual(producto.getStockActual() + mov.getCantidad());
        productoRepository.save(producto);

        // 2. Completar datos del movimiento y guardar
        mov.setFecha(LocalDateTime.now());
        mov.setEmpresa(producto.getEmpresa()); // Aseguramos el aislamiento SaaS
        return movimientoRepository.save(mov);
    }

    /**
     * Endpoint lógico para los despachos desde la App Android.
     * Mapea el estado 'ENTREGADO' de la tablet con una salida de inventario.
     */
    @Transactional
    public void procesarDespachoDesdeApp(Long productoId, Integer cantidad, String idDuelo, String loginOperativo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        InventarioMovimiento mov = new InventarioMovimiento();
        mov.setProducto(producto);
        mov.setEmpresa(producto.getEmpresa());
        mov.setCantidad(-cantidad); // Salida de inventario
        mov.setTipo("DESPACHO_MESA");
        // Guardamos quién lo hizo en la referencia para la auditoría
        mov.setReferenciaExterna("DUELO:" + idDuelo + " | OP:" + loginOperativo);
        
        registrarMovimiento(mov);
        String canalDestino = "/topic/empresa/" + producto.getEmpresa().getId() + "/dashboard";
        messagingTemplate.convertAndSend(canalDestino, "NUEVA_VENTA");
    }

    @Transactional(readOnly = true)
    public List<InventarioMovimiento> obtenerHistorialPorEmpresa(Long empresaId) {
        return movimientoRepository.findByEmpresaId(empresaId);
    }

    // --- NUEVO: ESTADÍSTICAS PARA EL DASHBOARD ---
    @Transactional(readOnly = true)
    public DashboardStatsDTO obtenerEstadisticasDashboard(Long empresaId) {
        return DashboardStatsDTO.builder()
                .totalProductos(productoRepository.countByEmpresaIdAndActivoTrue(empresaId))
                .productosBajoStock(productoRepository.countProductosBajoStock(empresaId))
                .terminalesActivas(terminalRepository.countBySuscripcionEmpresaIdAndBloqueadoFalse(empresaId))
                .personalActivo(usuarioOperativoRepository.countByEmpresaIdAndEstado(empresaId, EstadoUsuario.ACTIVO))
                .build();
    }

    // --- NUEVO: HISTORIAL / AUDITORÍA FORMATEADA ---
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerHistorialAuditoria(Long empresaId) {
        return movimientoRepository.findByEmpresaIdOrderByFechaDesc(empresaId).stream()
                .map(m -> MovimientoDTO.builder()
                        .id(m.getId())
                        .fecha(m.getFecha())
                        .tipo(m.getTipo())
                        .cantidad(m.getCantidad())
                        .productoNombre(m.getProducto() != null ? m.getProducto().getNombre() : "N/A")
                        .creador(m.getUsuario() != null ? m.getUsuario().getLogin() : "SISTEMA/APP")
                        .referencia(m.getReferenciaExterna())
                        .build())
                .toList();
    }
}