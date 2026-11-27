# TíoTec · API de evaluación y acciones preventivas

Proyecto Maven independiente con Spring Boot, Java 17 y H2. Desde `backend/`, ejecuta:

```sh
mvn spring-boot:run
```

Requiere JDK 17 o superior y Maven 3.6.3 o superior. La API escucha en `http://127.0.0.1:8080` y guarda el checklist en `data/tiotec.mv.db`. El servidor local mantiene un único checklist y comparte sus datos entre las llamadas. Antes de usarlo con varias personas, hará falta definir cómo identificar a cada una y separar sus datos.

## Acciones preventivas

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

## Evaluación

H2 conserva el catálogo y las respuestas en `data/tiotec.mv.db`. El cliente debe generar un UUID aleatorio una vez por instalación, conservarlo y reutilizarlo como `perfilId` en cada llamada. El UUID identifica los datos de la instalación y no autentica al cliente.

| Método y ruta | Cuerpo JSON | Respuesta |
| --- | --- | --- |
| GET /api/v1/preguntas | — | 200, las cuatro preguntas con `id`, `texto` y `orden`. |
| PUT /api/v1/evaluaciones/{perfilId}/respuestas/{preguntaId} | `{"respuesta":true}` | 200, por ejemplo `{"preguntaId":2,"respuesta":true}`. Devuelve 404 si la pregunta no existe y 400 si el UUID o el cuerpo son inválidos. |
| GET /api/v1/evaluaciones/{perfilId} | — | 200, un objeto con `perfilId`, `respuestas` y `preguntasPendientes`. Devuelve 400 si el UUID es inválido. |

`true` significa Sí y `false` significa No. Cada PUT crea o reemplaza la respuesta de esa pregunta. La consulta devuelve ambas listas en el orden del catálogo, con los IDs en `preguntasPendientes`. Un perfil nuevo obtiene `respuestas: []` y `preguntasPendientes: [1,2,3,4]`.
