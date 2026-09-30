package com.tienda.pedidos.service;
import org.springframework.stereotype.Service;
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void enviar(
        String correo,
        String asunto,
        String mensaje
    ){

        System.out.println("Enviando correo a: " + correo);
        System.out.println(asunto);
        System.out.println(mensaje);
    }
}
