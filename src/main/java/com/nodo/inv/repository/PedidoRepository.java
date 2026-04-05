package com.nodo.inv.repository;

import com.nodo.inv.entity.Empresa;
import com.nodo.inv.entity.Mesa;
import com.nodo.inv.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Este método es oro puro: Busca el "Carrito de compras" abierto de una mesa específica
    Optional<Pedido> findByEmpresaAndMesaAndEstado(Empresa empresa, Mesa mesa, String estado);
}