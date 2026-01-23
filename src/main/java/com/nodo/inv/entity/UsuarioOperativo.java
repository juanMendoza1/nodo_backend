package com.nodo.inv.entity;

import com.nodo.inv.Utils.EstadoUsuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inv_usuario_operativo")
public class UsuarioOperativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uop_ideregistro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_ideregistro", nullable = false)
    private Empresa empresa; // El slot pertenece a una empresa específica

    @Column(name = "uop_alias", nullable = false, length = 50)
    private String alias; // Ej: "MESERO 1", "CAJA PRINCIPAL", "BARRA"

    @Column(name = "uop_login", nullable = false, length = 50)
    private String login; // Credencial para la tablet

    @Column(name = "uop_password", nullable = false)
    private String password; // PIN o clave encriptada

    @Enumerated(EnumType.STRING)
    @Column(name = "uop_estado", nullable = false)
    private EstadoUsuario estado; // ACTIVO, INACTIVO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_ideregistro")
    private Rol rol; // Rol con permisos operativos limitados

    @Column(name = "uop_fecha_creacion")
    private LocalDateTime fechaCreacion;
}