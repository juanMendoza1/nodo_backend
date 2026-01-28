package com.nodo.inv.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nodo.inv.dto.ProductoDTO;
import com.nodo.inv.entity.Producto;
import com.nodo.inv.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

	private final ProductoRepository productoRepository;

    public List<ProductoDTO> obtenerCatalogoPorEmpresa(Long empresaId) {
        return productoRepository.findByEmpresaIdAndActivoTrue(empresaId)
                .stream()
                .map(ProductoDTO::new) // Usamos el constructor de mapeo
                .toList();
    }
}