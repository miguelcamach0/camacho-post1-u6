package com.tienda.pedidos.validacion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CadenaValidacionConfig {

    @Bean (name = "cadenaPrincipal")
    public ValidadorPedido cadenaValidacion(
            ValidadorStock stock,
            ValidadorCliente cliente,
            PromocionBlackFriday blackFriday,
            PromocionCorporativo corporativo,
            PromocionVolumen volumen
    ) {
        stock.encadenar(cliente);
        cliente.encadenar(blackFriday);
        blackFriday.encadenar(corporativo);
        corporativo.encadenar(volumen);
        return stock;
    }
}
