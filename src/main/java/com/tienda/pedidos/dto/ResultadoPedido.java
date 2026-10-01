package com.tienda.pedidos.dto;

public class ResultadoPedido {
    private boolean confirmado;
    private Long pedidoId;
    private double total;
    private String motivoRechazo;
    private double descuento;

    public static ResultadoPedido confirmado(Long pedidoId, double total, double descuento) {
        ResultadoPedido r = new ResultadoPedido();
        r.confirmado = true; r.pedidoId = pedidoId; r.total = total; r.descuento = descuento;
        return r;
    }
    public static ResultadoPedido rechazado(String motivo) {
        ResultadoPedido r = new ResultadoPedido();
        r.confirmado = false; r.motivoRechazo = motivo;
        return r;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public double getTotal() {
        return total;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public double getDescuento() {
        return descuento;
    }

    
}
