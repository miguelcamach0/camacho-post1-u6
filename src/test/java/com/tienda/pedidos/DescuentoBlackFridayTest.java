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


@SpringBootTest(properties = "promo.black-friday.activa=true")
class DescuentoBlackFridayTest {

    @Autowired
    private GestorPedidos gestorPedidos;


    @Test
    void pedidoEnBlackFridayAplicaDescuento() {

        PedidoRequest request = new PedidoRequest();

        request.setClienteId("C004");

        ItemPedido item = new ItemPedido();
        item.setProductoId(10L);
        item.setCantidad(1);

        request.setItems(List.of(item));


        ResultadoPedido resultado =
                gestorPedidos.procesarPedido(request);


        assertEquals(0.25, resultado.getDescuento());
    }
}
