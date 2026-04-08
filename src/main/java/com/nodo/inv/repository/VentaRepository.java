package com.nodo.inv.repository;

import com.nodo.inv.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByEmpresaId(Long empresaId);
    
    @Query("SELECT v FROM Venta v WHERE v.usuarioOperativo.id = :operarioId AND v.fecha >= :inicio AND v.fecha <= :fin")
    List<Venta> findByOperarioAndRangoFechas(@Param("operarioId") Long operarioId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}