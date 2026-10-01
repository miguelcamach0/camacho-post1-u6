package com.tienda.pedidos;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.service.GestorPedidos;


@SpringBootTest(properties = "promo.black-friday.activa=false")
class DescuentoVolumenTest {

    @Autowired
    private GestorPedidos gestorPedidos;


    @Test
    void pedidoMayor20UnidadesAplicaDescuentoVolumen() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C004");


        ItemPedido item1 = new ItemPedido();

        item1.setProductoId(10L);
        item1.setCantidad(16);


        ItemPedido item2 = new ItemPedido();

        item2.setProductoId(20L);
        item2.setCantidad(5);


        request.setItems(List.of(item1, item2));


        ResultadoPedido resultado =
                gestorPedidos.procesarPedido(request);


        assertEquals(0.12, resultado.getDescuento());
    }
}