package com.nodo.inv.service;

import com.nodo.inv.entity.InventarioMovimiento;
import com.nodo.inv.entity.Producto;
import com.nodo.inv.repository.InventarioMovimientoRepository;
import com.nodo.inv.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioMovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

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
        mov.setReferenciaExterna("DUELO:" + idDuelo);
        
        registrarMovimiento(mov);
    }

    @Transactional(readOnly = true)
    public List<InventarioMovimiento> obtenerHistorialPorEmpresa(Long empresaId) {
        return movimientoRepository.findByEmpresaId(empresaId);
    }
}