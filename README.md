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

### Parte 2 — Crecimiento del proyecto
**Antipatrón identificado:** Golden Hammer.
Las nuevas campañas de descuento ("PromocionBlackFriday", 
"PromocionCorporativo" y "PromocionVolumen") fueron implementadas como
nuevos eslabones dentro de la cadena "ValidadorPedido", reutilizando el
patrón Chain of Responsibility aplicado previamente para las validaciones
de stock y cliente.

Sin embargo, el nuevo problema no tenía la misma naturaleza que el
problema original. Los validadores iniciales requerían una secuencia
ordenada y posibilidad de corte anticipado: si "ValidadorStock" rechazaba
el pedido, "ValidadorCliente" no debía ejecutarse. En cambio, las clases
de promoción no presentan dependencia de orden entre ellas ni tienen la
responsabilidad de aceptar o rechazar un pedido.

La evidencia se encuentra en que las nuevas clases extienden
"ValidadorPedido", pero sus implementaciones no realizan validaciones ni
rechazos:

- "PromocionBlackFriday.ejecutar()" únicamente evalúa si la campaña está
  activa y modifica el descuento mediante "contexto.aplicarDescuentoCampana(0.25)".
- "PromocionCorporativo.ejecutar()" consulta el NIT del cliente y aplica un porcentaje de descuento cuando corresponde.
- "PromocionVolumen.ejecutar()" calcula la cantidad total de unidades y aplica un descuento si supera el límite establecido.

Ninguno de estos eslabones utiliza la capacidad principal de
"ValidadorPedido", que consiste en decidir si el flujo continúa o si el
pedido debe rechazarse.

Además, las tres promociones comparten un estado mutable dentro de
"ContextoPedido" mediante el atributo "descuentoCampana". Cada promoción
escribe sobre ese campo utilizando una regla de "el mayor descuento
gana", lo que acopla la resolución del descuento a la ejecución de la
cadena. Si en el futuro las reglas comerciales cambiaran y dos campañas
debieran combinarse, acumularse o aplicarse bajo una prioridad diferente,
la estructura actual no permitiría expresar claramente esa decisión.

La implementación se realizó porque Chain of Responsibility había
resuelto correctamente el problema anterior, pero no porque fuera la
abstracción más adecuada para las campañas promocionales. Se aplicó una
solución conocida a un problema con características diferentes, lo que
corresponde al antipatrón Golden Hammer.

**Decisión con justificación — Strategy en vez de nuevos eslabones de Chain of Responsibility**
Se corrigió la implementación modelando las campañas promocionales (Black Friday, Corporativo y Volumen) como nuevas estrategias de descuento (EstrategiaDescuento) en lugar de agregarlas como eslabones adicionales de la cadena de validación.

La decisión se tomó porque estas campañas tienen la misma naturaleza que las estrategias existentes (DescuentoVip y DescuentoFrecuente): calculan un porcentaje de descuento a partir de información del pedido o del cliente, pero no tienen la responsabilidad de validar condiciones que permitan rechazar el flujo ni dependen de un orden específico de ejecución. En cambio, los validadores ValidadorStock y ValidadorCliente sí justifican el uso de Chain of Responsibility, debido a que cada eslabón puede detener el procesamiento del pedido mediante un rechazo.

Mantener las campañas dentro de la cadena habría significado reutilizar un patrón conocido para un problema diferente, sin evaluar si sus características coincidían con el propósito original del patrón. Por esta razón, la alternativa fue descartada al ser la causa del antipatrón Golden Hammer identificado.

**Decisión con justificación — Eliminar código descartado en lugar de comentarlo**
Después de la corrección se eliminaron completamente las clases:
- PromocionBlackFriday
- PromocionCorporativo
- PromocionVolumen
- el atributo descuentoCampana del ContextoPedido.

No se conservaron como código comentado porque dejar implementaciones obsoletas dentro del proyecto puede generar confusión sobre su vigencia y dificultar futuras modificaciones. La trazabilidad del diseño anterior queda preservada mediante el historial de commits del repositorio, mientras que el código fuente mantiene únicamente la solución actualmente utilizada.


**Patrón aplicado:** 

Se implementó el patrón Strategy para encapsular las diferentes reglas de cálculo de descuentos dentro de estrategias independientes (DescuentoCorporativo, DescuentoBlackFriday, DescuentoVolumen, entre otras), evitando que la clase GestorPedidos tuviera que conocer o modificar directamente cada regla de negocio.

Inicialmente, el cálculo de descuentos presentaba una estructura rígida donde la incorporación de nuevas condiciones implicaba modificar clases existentes, aumentando el acoplamiento y dificultando la extensión del sistema.
La solución consistió en crear una abstracción común mediante EstrategiaDescuento, permitiendo que cada estrategia implemente su propio algoritmo de descuento. Posteriormente, CalculadorDescuentoFinal se encargó de coordinar estas estrategias y seleccionar el descuento aplicable mediante SelectorEstrategiaDescuento.

Además, se eliminaron los eslabones de una implementación previa de cadena de responsabilidad que no representaban una necesidad real del dominio, junto con el campo descuentoCampana, evitando conservar código muerto o estructuras sin uso que podrían convertirse en un antipatrón Lava Flow. El historial de estos cambios queda registrado únicamente en los commits del repositorio.

*Alternativa considerada: Chain of Responsibility*
Como alternativa se evaluó el patrón Chain of Responsibility, debido a que inicialmente el problema podía interpretarse como una cadena de validaciones donde cada regla de descuento tendría la oportunidad de aplicar una modificación al pedido.
Sin embargo, se descartó porque las reglas de descuento no requerían necesariamente una secuencia fija de procesamiento. El objetivo real era comparar diferentes estrategias disponibles y seleccionar la regla aplicable, no pasar la solicitud por una cadena donde cada elemento decidiera si continuar o detener el flujo.

## Cómo ejecutar
```
$ mvn spring-boot:run
$ mvn test
```

## Herramientas utilizadas
- Java 17, Spring Boot, Spring JDBC, Maven, H2 Database
- VS Code / IntelliJ IDEA, Git, GitHub

## Conclusiones
La actividad fue un proceso exigente y en varios momentos frustrante, especialmente al enfrentar errores de implementación y comprender cómo aplicar correctamente los patrones de diseño al código existente. Sin embargo, permitió comprender que refactorizar no consiste únicamente en modificar código, sino en analizar las responsabilidades, identificar problemas de diseño y justificar técnicamente cada decisión. También fue importante comprobar mediante pruebas que los cambios realizados mantuvieran el comportamiento esperado, lo que permitió desarrollar mayor confianza en el proceso de depuración y validación. En conjunto, ambas partes fortalecieron la comprensión de patrones como Chain of Responsibility y Strategy, así como la importancia de aplicar cada patrón cuando realmente aporta una solución al problema y no simplemente por utilizarlo.