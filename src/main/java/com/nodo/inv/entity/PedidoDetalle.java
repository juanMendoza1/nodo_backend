package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inv_pedido_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedidoDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    // Producto puede ser nulo si es un cobro "al vuelo" que no existe en el inventario (ej: Propina extra o un Tiempo de Billar calculado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto; 

    @Column(name = "nombre_item", nullable = false, length = 150)
    private String nombreItem; // Ej: "Cerveza Club Colombia" o "Tiempo Pool"

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // --- LA MAGIA PARA TU FRONTEND ---
    @Column(name = "tipo_item", nullable = false, length = 50)
    private String tipoItem; // "PRODUCTO_FISICO", "TIEMPO_MESA", "SERVICIO"

    @Column(name = "dueño_especifico", length = 100)
    private String dueñoEspecifico; // AQUÍ guardamos el nombre del cliente (ej: "Juan Mendoza"). Esto enlaza el producto a la CardView de React.

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado; // Para saber a qué hora exacta pidieron esta cerveza
}