DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS inventario;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS facturas;
DROP TABLE IF EXISTS clientes;


CREATE TABLE clientes (
    id VARCHAR(10) PRIMARY KEY,
    nombre VARCHAR(100),
    tipo_cliente VARCHAR(20),
    nit VARCHAR(20)
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
    cliente_id VARCHAR(10),
    monto DOUBLE,
    pagada BOOLEAN
);


CREATE TABLE pedidos(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id VARCHAR(10),
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