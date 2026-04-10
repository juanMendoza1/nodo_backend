package com.nodo.inv.repository;

import com.nodo.inv.entity.ConsecutivoDocumento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsecutivoDocumentoRepository extends JpaRepository<ConsecutivoDocumento, Long> {
    
    // 🔥 MAGIA PURA: El PESSIMISTIC_WRITE bloquea la fila en la BD. 
    // Nadie más puede leer ni tocar este consecutivo hasta que la factura actual termine de guardarse.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConsecutivoDocumento c WHERE c.empresa.id = :empresaId AND c.tipoDocumento.id = :tipoDocId")
    Optional<ConsecutivoDocumento> findByEmpresaAndTipoDocumentoForUpdate(
            @Param("empresaId") Long empresaId, 
            @Param("tipoDocId") Long tipoDocId);
}