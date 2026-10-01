package com.tienda.pedidos.descuento;

import com.tienda.pedidos.validacion.ContextoPedido;

// tomando el mayor -- la misma regla de negocio que antes, sin escribir en un campo
// mutable compartido desde clases que no son responsables de validar nada
@org.springframework.stereotype.Component
public class CalculadorDescuentoFinal {

        private final SelectorEstrategiaDescuento selectorPorCliente;
        private final java.util.List<EstrategiaDescuento> campanas;
        private final DescuentoVolumen volumen;

        public CalculadorDescuentoFinal(
                        SelectorEstrategiaDescuento selectorPorCliente,
                        DescuentoBlackFriday blackFriday,
                        DescuentoCorporativo corporativo,
                        DescuentoVolumen volumen) {
                this.selectorPorCliente = selectorPorCliente;

                this.campanas = java.util.List.of(
                                blackFriday,
                                corporativo);

                this.volumen = volumen;
        }

        public double calcular(ContextoPedido contexto) {

                double descuentoCliente = selectorPorCliente
                                .seleccionar(contexto.getTipoCliente())
                                .calcular(contexto);

                double descuentoVolumen = volumen.calcular(contexto);

                double descuentoCampana = 0;

                for (EstrategiaDescuento campana : campanas) {
                        descuentoCampana = Math.max(
                                        descuentoCampana,
                                        campana.calcular(contexto));
                }

                return Math.max(
                                descuentoCliente,
                                Math.max(descuentoVolumen, descuentoCampana));
        }
}