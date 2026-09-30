package com.tienda.pedidos;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.service.GestorPedidos;

@SpringBootTest
class GestorPedidosTest {

    @Autowired
    private GestorPedidos gestorPedidos;

    @Test
    void pedidoRechazadoPorStockInsuficiente() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C001");
        request.setClienteEmail("vip@test.com");

        ItemPedido item = new ItemPedido();

        item.setProductoId(10L);
        item.setCantidad(100);

        request.setItems(List.of(item));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertFalse(resultado.isConfirmado());

        assertTrue(
                resultado.getMotivoRechazo()
                        .contains("Stock insuficiente"));

    }

    @Test
    void clienteNoRegistrado() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C999");
        request.setClienteEmail("cliente@test.com");

        ItemPedido item = new ItemPedido();

        item.setProductoId(10L);
        item.setCantidad(1);

        request.setItems(List.of(item));

        assertThrows(
                org.springframework.dao.EmptyResultDataAccessException.class,
                () -> gestorPedidos.procesarPedido(request));
    }

    @Test
    void clienteMorosoAntesDeCorte() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C003");
        request.setClienteEmail("moroso@test.com");

        ItemPedido item = new ItemPedido();

        item.setProductoId(10L);
        item.setCantidad(1);

        request.setItems(List.of(item));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertFalse(resultado.isConfirmado());

        assertTrue(
                resultado.getMotivoRechazo()
                        .contains("Cliente con deuda pendiente"));
    }

    @Test
    void clienteVipAplicaDescuento() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C001");
        request.setClienteEmail("vip@test.com");

        ItemPedido item = new ItemPedido();

        item.setProductoId(20L);
        item.setCantidad(2);

        request.setItems(List.of(item));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());

        assertNotNull(resultado.getPedidoId());

    }

    @Test
    void clienteFrecuenteAplicaDescuento() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C002");
        request.setClienteEmail("frecuente@test.com");

        ItemPedido item = new ItemPedido();

        item.setProductoId(10L);
        item.setCantidad(1);

        request.setItems(List.of(item));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());

    }

}