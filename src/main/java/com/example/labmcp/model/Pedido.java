// Autor: Diego Alexander Sánchez
package com.example.labmcp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cliente;
    private String direccion;
    private String estado;   // ABIERTO, CONFIRMADO, CANCELADO
    private Double total;
    private String fecha;

    public Pedido() {}

    public Pedido(String cliente, String direccion) {
        this.cliente = cliente;
        this.direccion = direccion;
        this.estado = "ABIERTO";
        this.total = 0.0;
        this.fecha = LocalDate.now().toString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
