package com.nodo.inv.retail.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nodo.inv.retail.entity.VentaDetalle;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {
}