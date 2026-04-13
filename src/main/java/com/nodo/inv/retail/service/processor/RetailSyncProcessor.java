package com.nodo.inv.retail.service.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.inv.core.processor.OperacionDomainProcessor;

import java.util.Map;

// 🔥 LA MAGIA: El nombre del Bean coincide con el 'serviceProcessorBean' de la tabla de Dominios
@Service("retailSyncProcessor") 
@RequiredArgsConstructor
public class RetailSyncProcessor implements OperacionDomainProcessor {

    // Aquí inyectarás tus repositorios operativos: VentaRepository, DetalleVentaRepository, etc.
    // private final VentaRepository ventaRepository;

    @Override
    @Transactional
    public void procesarSincronizacion(Long empresaId, Map<String, Object> payload) {
        System.out.println("🛒 [RETAIL PROCESSOR] Recibiendo datos de la empresa: " + empresaId);
        
        // 1. Extraer los datos de la "caja negra" que mandó la tablet de Retail
        // Ej: List<Map<String, Object>> ventas = (List) payload.get("ventas");
        
        // 2. Aquí va toda la lógica transaccional hacia las tablas inv_venta, inv_detalle...
        System.out.println("✅ Guardando mesas, propinas e ítems en las tablas inv_...");
        
        // 3. (Opcional) Si la venta está cerrada, inyectar directamente al Libro de Documentos (con_documento)
    }
}