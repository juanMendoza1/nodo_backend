package com.nodo.inv.retail.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.nodo.inv.retail.entity.Producto;

@Data
@NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private Long categoriaId;
    private String categoriaNombre;
    private Long unidadMedidaId;
    private String unidadMedidaNombre;
    
    // Precios para la lógica de distribución en la App
    private BigDecimal precioCosto;  // Necesario para cálculos de "daño" o utilidad
    private BigDecimal precioVenta;  // Precio al público
    
    private Integer stockActual;
    private Boolean activo;

    public ProductoDTO(Producto p) {
        this.id = p.getId();
        this.codigo = p.getCodigo();
        this.nombre = p.getNombre();
        
        // Mapeo de precios desde la entidad
        this.precioCosto = p.getPrecioCosto(); 
        this.precioVenta = p.getPrecioVenta();
        
        this.stockActual = p.getStockActual();
        this.activo = p.getActivo();
        
        if (p.getCategoria() != null) {
            this.categoriaId = p.getCategoria().getId();
            this.categoriaNombre = p.getCategoria().getNombre();
        }
        
        if (p.getUnidadMedida() != null) {
            this.unidadMedidaId = p.getUnidadMedida().getId();
            this.unidadMedidaNombre = p.getUnidadMedida().getNombre();
        }
    }
}