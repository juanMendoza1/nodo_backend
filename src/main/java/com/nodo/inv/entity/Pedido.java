package com.nodo.inv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inv_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    // --- LLAVES UNIVERSALES ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_programa", nullable = false)
    private Programa programa; // Define si viene de Billar, Restaurante, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_terminal_dispositivo", nullable = false)
    private TerminalDispositivo terminal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_operario", nullable = false)
    private UsuarioOperativo operario; // El cajero o mesero

    // --- COLUMNAS OPERATIVAS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesa")
    private Mesa mesa; // Puede ser nulo si es venta de mostrador (sin mesa)

    @Column(name = "tipo_comanda", nullable = false, length = 50)
    private String tipoComanda; // Ej: "VENTA_REGULAR", "DUELO"

    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // Ej: "ABIERTA", "CERRADA", "ANULADA"

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "total_calculado", precision = 12, scale = 2)
    private BigDecimal totalCalculado;

    // --- RELACIÓN CON EL DETALLE ---
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    // Método helper para mantener la sincronía bidireccional
    public void addDetalle(PedidoDetalle detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }
    
    @Version
    @Column(name = "version")
    private Long version;
}