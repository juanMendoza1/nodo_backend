package com.nodo.inv.service;

import com.nodo.inv.core.entity.DominioOperativo;
import com.nodo.inv.core.entity.Programa;
import com.nodo.inv.service.processor.OperacionDomainProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SyncDispatcherService {

    // 🔥 ApplicationContext es el "Directorio" donde Spring guarda a todos los especialistas
    private final ApplicationContext applicationContext;

    public void enrutarPaquete(Programa programa, Long empresaId, Map<String, Object> payload) {
        
        DominioOperativo dominio = programa.getDominio();
        
        if (dominio == null || !dominio.getActivo()) {
            throw new RuntimeException("El Programa no tiene un Dominio Operativo válido o está inactivo.");
        }

        String nombreBean = dominio.getServiceProcessorBean(); // Ej: "retailSyncProcessor"

        try {
            // 1. Buscamos al especialista en el directorio de Spring por su nombre
            OperacionDomainProcessor especialista = applicationContext.getBean(nombreBean, OperacionDomainProcessor.class);
            
            // 2. Le entregamos la caja negra para que haga su magia
            especialista.procesarSincronizacion(empresaId, payload);
            
        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
            // Si el SuperAdmin escribió mal el nombre del Bean en React, fallará aquí.
            throw new RuntimeException("CRÍTICO: No se encontró el especialista para el dominio " + dominio.getCodigo() + ". Verifique el nombre del Bean: " + nombreBean);
        }
    }
}