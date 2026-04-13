package com.nodo.inv.retail.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Unidad;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.UnidadRepository;
import com.nodo.inv.retail.dto.ProductoDTO;
import com.nodo.inv.retail.entity.Producto;
import com.nodo.inv.retail.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;
    private final UnidadRepository unidadRepository;

    public List<ProductoDTO> obtenerCatalogoPorEmpresa(Long empresaId) {
        // Traemos todos (activos e inactivos) para que el admin pueda gestionarlos
        return productoRepository.findByEmpresaId(empresaId) // Tendrás que crear este método en tu repositorio
                .stream()
                .map(ProductoDTO::new)
                .toList();
    }

 // Fíjate que ahora retorna ProductoDTO en vez de Producto
    @Transactional
    public ProductoDTO guardarProducto(Long empresaId, ProductoDTO dto) {
        Producto producto;

        if (dto.getId() != null) {
            // Actualizar existente
            producto = productoRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        } else {
            // Crear nuevo
            producto = new Producto();
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            producto.setEmpresa(empresa);
            producto.setActivo(true);
        }

        // Mapear datos básicos
        producto.setCodigo(dto.getCodigo());
        producto.setNombre(dto.getNombre());
        producto.setPrecioCosto(dto.getPrecioCosto());
        producto.setPrecioVenta(dto.getPrecioVenta());
        // Solo actualizamos stock si no es nulo
        producto.setStockActual(dto.getStockActual() != null ? dto.getStockActual() : producto.getStockActual());
        
        // Mapear parámetros dinámicos (Categoría y Medida)
        if (dto.getCategoriaId() != null) {
            Unidad categoria = unidadRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        if (dto.getUnidadMedidaId() != null) {
            Unidad medida = unidadRepository.findById(dto.getUnidadMedidaId())
                    .orElseThrow(() -> new RuntimeException("Unidad de medida no encontrada"));
            producto.setUnidadMedida(medida);
        }

        // Guardamos en BD
        Producto productoGuardado = productoRepository.save(producto);
        
        // 🔥 LA MAGIA ESTÁ AQUÍ: Convertimos la Entidad al DTO seguro antes de enviarlo a React
        return new ProductoDTO(productoGuardado); 
    }

    @Transactional
    public void cambiarEstado(Long idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Soft delete: solo lo inactivamos
        producto.setActivo(!producto.getActivo());
        productoRepository.save(producto);
    }
}