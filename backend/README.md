# TíoTec · API de acciones preventivas

Proyecto Maven independiente con Spring Boot, Java 17 y H2. Desde `backend/`, ejecuta:

```sh
mvn spring-boot:run
```

Requiere JDK 17 o superior y Maven 3.6.3 o superior. La API escucha en `http://127.0.0.1:8080` y guarda el checklist en `data/tiotec.mv.db`. El servidor local mantiene un único checklist y comparte sus datos entre las llamadas. Antes de usarlo con varias personas, hará falta definir cómo identificar a cada una y separar sus datos.

## Contrato REST

Ruta base `/api/v1/acciones-preventivas`. El cliente asigna `id` a partir del índice de la acción en la evaluación. Cada fila guarda ese índice y el booleano `completada`. Los textos de las acciones siguen definidos en la app.

| Método y ruta | Cuerpo JSON | Respuesta |
| --- | --- | --- |
| POST / | `{"id":0,"completada":false}` | 201, acción creada y cabecera Location. 409 si el ID existe. |
| GET / | — | 200, lista ordenada por ID. Devuelve `[]` si está vacía. |
| GET /{id} | — | 200, acción consultada. |
| PUT /{id} | `{"completada":true}` | 200, acción actualizada. El ID se toma de la URL. |
| DELETE /{id} | — | 204, acción eliminada. |

Usa la ruta base sin barra final para POST y GET de la colección. Las operaciones sobre un ID inexistente devuelven 404. Los cuerpos inválidos devuelven 400.

Por ejemplo, la app puede crear la acción `0` pendiente, marcarla más tarde con `PUT /api/v1/acciones-preventivas/0` y recibir `{"id":0,"completada":true}`. Para restaurar la vista tras abrir la app, puede consultar la lista y aplicar cada valor al índice correspondiente. Una acción ausente se puede tratar como pendiente, igual que al inicializar el checklist local.
