CREATE TABLE clientes(
 id BIGINT PRIMARY KEY,
 tipo_cliente VARCHAR(20)
);


CREATE TABLE productos(
 id BIGINT PRIMARY KEY,
 precio DOUBLE
);


CREATE TABLE inventario(
 producto_id BIGINT,
 stock INTEGER
);


CREATE TABLE facturas(
 cliente_id BIGINT,
 monto DOUBLE,
 pagada BOOLEAN
);


CREATE TABLE pedidos(
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 cliente_id BIGINT,
 subtotal DOUBLE,
 descuento DOUBLE,
 impuesto DOUBLE,
 total DOUBLE,
 fecha TIMESTAMP,
 estado VARCHAR(20)
);


CREATE TABLE detalle_pedido(
 pedido_id BIGINT,
 producto_id BIGINT,
 cantidad INTEGER
);