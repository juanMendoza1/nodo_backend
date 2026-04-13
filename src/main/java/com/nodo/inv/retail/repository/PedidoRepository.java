package com.nodo.inv.retail.repository;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.retail.entity.Mesa;
import com.nodo.inv.retail.entity.Pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Este método es oro puro: Busca el "Carrito de compras" abierto de una mesa específica
    Optional<Pedido> findByEmpresaAndMesaAndEstado(Empresa empresa, Mesa mesa, String estado);
    List<Pedido> findByOperarioIdAndEstadoAndFechaCierreBetween(Long operarioId, String estado, LocalDateTime inicio, LocalDateTime fin);
}