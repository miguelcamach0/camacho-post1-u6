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

**Patrón aplicado:** 
Se aplicó **Chain of Responsability** por que las validaciones tienen una característica fundamental: existe una dependencia de orden y un corte anticipado del flujo, por ejemplo: si un pedido no tiene stock disponible, no tiene sentido consultar las condiciones del cliente o también, si el cliente no existe, no debería de continuar haciendo validaciones posteriores. La aplicación de este patrón nos permite que cada validador sea responsable de una única regla y decida explícitamente si la delega la ejecución al siguiente elemento, la validación dejó de ser una responsabilidad interna de GestorPedidos y pasó a componentes especializados, reduciendo el acoplamiento y facilitando agregar nuevas reglas sin modificar el flujo principal.

Se aplicó también **Strategy** porque los descuentos representan algoritmos intercambiables, no son validaciones. Cada descuento: recibe un contexto, aplica una fórmula distinta y produce un resultado independiente. Además, gracias a Strategy podemos extender el sistema agregando una nueva clase como por ejemplo un descuento corporativo sin modificar las estrategias existentes. Esto también permitiría el cumplimiento de OCP, el sistema queda abierto a nuevos descuentos pero cerrado a modificaciones sobre el código probado.

**Otras decisiones tomadas**
- Se creó "PedidoRepository" como capa encargada del acceso a datos, permitiendo separar la lógica de negocio de los detalles de almacenamiento.

- Servicio de notificación separado, la notificación es una responsabilidad diferente al procesamiento del pedido, al separarla se puede cambiar un aviso por correo a SMS o Whatsapp, también se podría modificar el formato del mensaje.

Alternativa descartada:
- Implementar una colección de validadores mediante List<Predicate<ContextoPedido>> o un método "validarTodo()", aunque inicialmente permite separar código, nos limitamos a que la lógica de continuación queda centralizada, no permite que un validador controle explícitamente si la cadena debe detenerse o incluso puede terminar convirtiendo el flujo nuevamente en una secuencia rígida.
- Para los descuentos consideré Chain of Responsabiliti, pero no era adecuado porque una cadena representa una serie de pasos donde varios componentes deciden si procesar o rechazar una solicitud, para el caso de los descuentos no maneja ese flujo (Violaría OCP).
-Mantener el JdbcTemplate dentro de GestorPedidos, pero hubiese conservado aún el problema original.
- Mantener la notificación dentro del servicio principal era más sencillo inicialmente, pero aumenta el acoplamiento y contradice SRP.
