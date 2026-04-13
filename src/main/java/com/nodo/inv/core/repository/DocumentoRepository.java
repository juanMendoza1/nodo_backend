package com.nodo.inv.core.repository;

// 🔥 IMPORTS CORRECTOS PARA PAGINACIÓN EN SPRING BOOT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nodo.inv.core.entity.Documento;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    // Obtener todo el historial financiero de un local (Lista completa sin paginar)
    List<Documento> findByEmpresaIdOrderByFechaEmisionDesc(Long empresaId);

    // 🔥 NUEVO: Obtener el historial financiero de un local (CON PAGINACIÓN DE SERVIDOR)
    Page<Documento> findByEmpresaIdOrderByFechaEmisionDesc(Long empresaId, Pageable pageable);

    // Obtener los documentos a nombre de un cliente/operario (Útil para sacar estado de cuenta)
    List<Documento> findByEmpresaIdAndTerceroIdOrderByFechaEmisionDesc(Long empresaId, Long terceroId);

    // CONSULTA DE CARTERA: Tráeme las facturas que todavía me deben plata
    @Query("SELECT d FROM Documento d WHERE d.empresa.id = :empresaId AND d.saldoDocumento > 0 AND d.estado = 'EMITIDO'")
    List<Documento> findDocumentosConSaldoPendiente(@Param("empresaId") Long empresaId);

    // Rastrear el árbol de documentos (Ej: Buscar todos los abonos o notas de una Factura)
    List<Documento> findByDocumentoPadreIdAndEstado(Long documentoPadreId, String estado);
    
    java.util.Optional<Documento> findByEmpresaIdAndConsecutivo(Long empresaId, String consecutivo);
}