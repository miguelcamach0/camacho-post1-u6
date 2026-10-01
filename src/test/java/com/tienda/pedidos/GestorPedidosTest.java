package com.tienda.pedidos;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.service.GestorPedidos;

@SpringBootTest
@TestMethodOrder(MethodOrderer.Random.class)
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    @Test
    void aplicaBlackFriday() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C001");

        ItemPedido item = new ItemPedido();
        item.setProductoId(10L);
        item.setCantidad(1);

        request.setItems(List.of(item));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());

        assertEquals(0.25, resultado.getDescuento());
    }

    @Test
    void aplicaDescuentoVolumen() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C004");

        ItemPedido item1 = new ItemPedido();
        item1.setProductoId(10L);
        item1.setCantidad(16);

        ItemPedido item2 = new ItemPedido();
        item2.setProductoId(20L);
        item2.setCantidad(5);

        request.setItems(List.of(item1, item2));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertEquals(0.25, resultado.getDescuento());
    }

    @Test
    void aplicaDescuentoCorporativo() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C005");

        ItemPedido item = new ItemPedido();

        item.setProductoId(10L);
        item.setCantidad(1);

        request.setItems(List.of(item));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        assertTrue(resultado.isConfirmado());

        assertEquals(0.25, resultado.getDescuento());
    }

    @Test
    void calculaMismoDescuentoQueReglasAnteriores() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C004");

        ItemPedido item1 = new ItemPedido();
        item1.setProductoId(10L);
        item1.setCantidad(16);

        ItemPedido item2 = new ItemPedido();
        item2.setProductoId(20L);
        item2.setCantidad(5);

        request.setItems(List.of(item1, item2));

        ResultadoPedido resultado = gestorPedidos.procesarPedido(request);

        // Antes Paso 5 daba 25% por Black Friday
        // Ahora CalculadorDescuentoFinal debe mantenerlo

        assertEquals(0.25, resultado.getDescuento());
    }

}