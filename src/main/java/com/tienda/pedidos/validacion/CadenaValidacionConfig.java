package com.tienda.pedidos.validacion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CadenaValidacionConfig {

    @Bean("cadenaValidacion")
    public ValidadorPedido cadenaValidacion(
            ValidadorStock stock,
            ValidadorCliente cliente
    ) {

        stock.encadenar(cliente);

        return stock;
    }
}
