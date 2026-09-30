package com.tienda.pedidos.service;

public interface EmailService {
    void enviar(
        String correo,
        String asunto,
        String mensaje
    );
}
