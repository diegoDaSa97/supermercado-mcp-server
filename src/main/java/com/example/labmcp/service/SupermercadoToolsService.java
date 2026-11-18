// Autor: Diego Alexander Sánchez
package com.example.labmcp.service;

import com.example.labmcp.model.Pedido;
import com.example.labmcp.model.PedidoItem;
import com.example.labmcp.model.Producto;
import com.example.labmcp.repository.PedidoItemRepository;
import com.example.labmcp.repository.PedidoRepository;
import com.example.labmcp.repository.ProductoRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SupermercadoToolsService {

    private final ProductoRepository productos;
    private final PedidoRepository pedidos;
    private final PedidoItemRepository items;

    public SupermercadoToolsService(ProductoRepository productos,
                                    PedidoRepository pedidos,
                                    PedidoItemRepository items) {
        this.productos = productos;
        this.pedidos = pedidos;
        this.items = items;
    }

    @Tool(description = "Lista todos los productos del supermercado con precio y stock.")
    public List<Producto> listarProductos() {
        return productos.findAll();
    }

    @Tool(description = "Busca productos por fragmento del nombre, por ejemplo 'leche'.")
    public List<Producto> buscarProductos(
            @ToolParam(description = "Fragmento del nombre del producto") String fragmento) {
        return productos.findByNombreContainingIgnoreCase(fragmento);
    }

    @Tool(description = "Lista los productos de una categoría (Lácteos, Frutas, Granos, Panadería, Huevos, Aseo).")
    public List<Producto> listarPorCategoria(
            @ToolParam(description = "Nombre de la categoría") String categoria) {
        return productos.findByCategoriaIgnoreCase(categoria);
    }

    @Tool(description = "Crea un pedido a domicilio vacío (estado ABIERTO) para un cliente y una dirección. Devuelve el pedido con su id.")
    @Transactional
    public Pedido crearPedido(
            @ToolParam(description = "Nombre del cliente") String cliente,
            @ToolParam(description = "Dirección de entrega") String direccion) {
        if (cliente == null || cliente.isBlank() || direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Cliente y dirección son obligatorios");
        }
        return pedidos.save(new Pedido(cliente.trim(), direccion.trim()));
    }

    @Tool(description = "Agrega un producto con su cantidad a un pedido ABIERTO. Valida el stock y lo descuenta. Devuelve el pedido actualizado.")
    @Transactional
    public Map<String, Object> agregarProducto(
            @ToolParam(description = "Id del pedido") Long pedidoId,
            @ToolParam(description = "Id del producto") Long productoId,
            @ToolParam(description = "Cantidad, mayor que 0") Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        Pedido p = buscarPedido(pedidoId);
        if (!"ABIERTO".equals(p.getEstado())) {
            throw new IllegalArgumentException("El pedido no está abierto");
        }
        Producto prod = productos.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto " + productoId));
        if (prod.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + prod.getStock());
        }
        prod.setStock(prod.getStock() - cantidad);
        productos.save(prod);
        double subtotal = prod.getPrecio() * cantidad;
        items.save(new PedidoItem(pedidoId, productoId, cantidad, subtotal));
        p.setTotal(p.getTotal() + subtotal);
        pedidos.save(p);
        return verPedido(pedidoId);
    }

    @Tool(description = "Muestra un pedido con sus productos y el total.")
    public Map<String, Object> verPedido(
            @ToolParam(description = "Id del pedido") Long pedidoId) {
        Pedido p = buscarPedido(pedidoId);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("pedido", p);
        r.put("items", items.findByPedidoId(pedidoId));
        return r;
    }

    @Tool(description = "Lista los pedidos de un cliente por su nombre.")
    public List<Pedido> listarPedidosCliente(
            @ToolParam(description = "Nombre del cliente") String cliente) {
        return pedidos.findByClienteIgnoreCase(cliente);
    }

    @Tool(description = "Confirma un pedido ABIERTO que tenga al menos un producto. Pasa a CONFIRMADO.")
    @Transactional
    public Pedido confirmarPedido(
            @ToolParam(description = "Id del pedido") Long pedidoId) {
        Pedido p = buscarPedido(pedidoId);
        if (!"ABIERTO".equals(p.getEstado())) {
            throw new IllegalArgumentException("Solo se confirman pedidos ABIERTOS");
        }
        if (items.findByPedidoId(pedidoId).isEmpty()) {
            throw new IllegalArgumentException("El pedido no tiene productos");
        }
        p.setEstado("CONFIRMADO");
        return pedidos.save(p);
    }

    @Tool(description = "Cancela un pedido ABIERTO o CONFIRMADO y devuelve el stock. Pedir confirmación al usuario antes de usarla.")
    @Transactional
    public Pedido cancelarPedido(
            @ToolParam(description = "Id del pedido") Long pedidoId) {
        Pedido p = buscarPedido(pedidoId);
        if ("CANCELADO".equals(p.getEstado())) {
            throw new IllegalArgumentException("El pedido ya está cancelado");
        }
        for (PedidoItem it : items.findByPedidoId(pedidoId)) {
            productos.findById(it.getProductoId()).ifPresent(prod -> {
                prod.setStock(prod.getStock() + it.getCantidad());
                productos.save(prod);
            });
        }
        p.setEstado("CANCELADO");
        return pedidos.save(p);
    }

    private Pedido buscarPedido(Long id) {
        return pedidos.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido " + id));
    }
}