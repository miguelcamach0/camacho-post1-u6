# Post-contenido — Unidad 6: Antipatrones de Diseño

## Descripción
Repositorio del post-contenido de la Unidad 6 de Patrones de Diseño
de Software — Sexto Semestre. Un único proyecto Spring Boot
(pedidos-service/) con dos partes: diagnóstico y refactorización de
un antipatrón combinado en GestorPedidos, y diagnóstico y corrección
de un segundo antipatrón introducido al hacer crecer el mismo
proyecto con tres campañas de descuento.

## Decisiones de diseño

### Parte 1 — GestorPedidos
**Antipatrón identificado:** 
**God Object y Spaghetti code combinados**
La clase GestorPedidos concentra múltiples responsabilidades que pertenecen a diferentes componentes del sistema. El método procesarPedido() mezcla validaciones, reglas de negocio, el acceso a datos, la persistencia y la comunicación externa:

- Entre las líneas 24-38, hace validación de existencia de productos e inventario.
- Entre las líneas 41-63, consulta clientes y facturas, además de reglas de negocio relacionadas con clientes morosas y horario de corte.
- Entre las líneas 66-73, consulta del precio de cada producto directamente desde la base de datos y cálculo dentro del mismo método (subtotal).
- Entre las líneas 76-95, tiene la aplicación de descuentos con reglas condicionales para clientes VIP y FRECUENTE con múltiples niveles de decisión. También es importante recalcar que entre estas líneas se viola el principio Open/Closed (OCP), ya que las reglas de descuento no son extensibles, actualmente para agregar un nuevo tipo de cliente sería necesario modificar el método agregando nuevos if/else, lo que también generaría complejidad condicional.
- Entre las líneas 102-117 hay persistencia del pedido e inventario: inserción del pedido, creación del detalle y la actualización del stock mediante SQL embebido.
- Entre las líneas 120-134 se construye el cuerpo del correo y se envía mediante emailService.

**Otros puntos identificados**
- Existe alto acoplamiento, hay una dependencia directa de la persistencia. GestorPedidos conoce directamente la estructura de la base de datos mediante JdbcTemplate, también ejecuta directamente consultas SQL entre las líneas 27-29 y las líneas 102-106. Este problema está relacionado con una violación del Dependency Inversion Principle (DIP), debido a que el servicio depende directamente de una implementación concreta (JdbcTemplate) en lugar de una abstracción de persistencia.


