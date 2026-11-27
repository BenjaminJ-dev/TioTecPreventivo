package com.example.tiotec.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluacionControllerTest {
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static ConfigurableApplicationContext backend;
    private static String url;

    @TempDir
    static Path directorio;

    @BeforeAll
    static void iniciar() {
        backend = SpringApplication.run(TioTecBackendApplication.class,
                "--server.port=0", "--spring.datasource.url=jdbc:h2:file:" + directorio.resolve("tiotec"));
        url = "http://127.0.0.1:" + backend.getEnvironment().getProperty("local.server.port") + "/api/v1";
    }

    @AfterAll
    static void detener() {
        if (backend != null) {
            backend.close();
        }
    }

    @Test
    void listaLasCuatroPreguntasEnElOrdenDeLaApp() throws Exception {
        assertEquals(JSON.readTree("""
                [
                  {"id":1,"texto":"¿Realizas copias de seguridad de tus archivos importantes?","orden":1},
                  {"id":2,"texto":"¿Mantienes actualizado tu teléfono y sus aplicaciones?","orden":2},
                  {"id":3,"texto":"¿Usas una contraseña o método adicional de verificación en tus cuentas?","orden":3},
                  {"id":4,"texto":"¿Tienes actualizado un correo o teléfono para recuperar tus cuentas?","orden":4}
                ]
                """), get("/preguntas", 200));
    }

    @Test
    void guardaActualizaYRepiteLaMismaRespuestaSinDuplicarla() throws Exception {
        UUID perfil = UUID.randomUUID();
        String evaluacion = "/evaluaciones/" + perfil;
        String pregunta = evaluacion + "/respuestas/2";

        assertEquals(JSON.readTree("""
                {"preguntaId":2,"respuesta":true}
                """), put(pregunta, "{\"respuesta\":true}", 200));
        comprobarEvaluacion(perfil, "[{\"preguntaId\":2,\"respuesta\":true}]", "[1,3,4]");

        JsonNode actualizada = JSON.readTree("{\"preguntaId\":2,\"respuesta\":false}");
        assertEquals(actualizada, put(pregunta, "{\"respuesta\":false}", 200));
        assertEquals(actualizada, put(pregunta, "{\"respuesta\":false}", 200));
        comprobarEvaluacion(perfil, "[{\"preguntaId\":2,\"respuesta\":false}]", "[1,3,4]");
        comprobarEvaluacion(UUID.randomUUID(), "[]", "[1,2,3,4]");
    }

    @Test
    void detectaPendientesDesdeUnaEvaluacionVaciaHastaUnaCompleta() throws Exception {
        UUID perfil = UUID.randomUUID();
        comprobarEvaluacion(perfil, "[]", "[1,2,3,4]");

        for (int id : new int[] {4, 2, 3, 1}) {
            put("/evaluaciones/" + perfil + "/respuestas/" + id, "{\"respuesta\":false}", 200);
        }
        comprobarEvaluacion(perfil, """
                [
                  {"preguntaId":1,"respuesta":false},
                  {"preguntaId":2,"respuesta":false},
                  {"preguntaId":3,"respuesta":false},
                  {"preguntaId":4,"respuesta":false}
                ]
                """, "[]");
    }

    @Test
    void rechazaPreguntasInexistentesRespuestasInvalidasYPerfilesSinUuid() throws Exception {
        UUID perfil = UUID.randomUUID();
        String respuestas = "/evaluaciones/" + perfil + "/respuestas/";
        put(respuestas + "99", "{\"respuesta\":true}", 404);
        put(respuestas + "1", "{}", 400);
        put(respuestas + "1", "{\"respuesta\":null}", 400);
        put(respuestas + "1", "{\"respuesta\":\"false\"}", 400);
        put(respuestas + "1", "{\"respuesta\":1}", 400);
        get("/evaluaciones/NombreVisible", 400);
        put("/evaluaciones/NombreVisible/respuestas/1", "{\"respuesta\":true}", 400);
        comprobarEvaluacion(perfil, "[]", "[1,2,3,4]");
    }

    @Test
    void conservaLaRespuestaAlReiniciarElBackend() throws Exception {
        UUID perfil = UUID.randomUUID();
        put("/evaluaciones/" + perfil + "/respuestas/3", "{\"respuesta\":true}", 200);

        detener();
        iniciar();

        comprobarEvaluacion(perfil, "[{\"preguntaId\":3,\"respuesta\":true}]", "[1,2,4]");
    }

    private static void comprobarEvaluacion(UUID perfil, String respuestas, String pendientes) throws Exception {
        assertEquals(JSON.readTree("""
                {"perfilId":"%s","respuestas":%s,"preguntasPendientes":%s}
                """.formatted(perfil, respuestas, pendientes)), get("/evaluaciones/" + perfil, 200));
    }

    private static JsonNode get(String ruta, int estado) throws Exception {
        return enviar(HttpRequest.newBuilder(URI.create(url + ruta)).GET().build(), estado);
    }

    private static JsonNode put(String ruta, String cuerpo, int estado) throws Exception {
        return enviar(HttpRequest.newBuilder(URI.create(url + ruta))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build(), estado);
    }

    private static JsonNode enviar(HttpRequest peticion, int estado) throws Exception {
        var respuesta = CLIENTE.send(peticion, HttpResponse.BodyHandlers.ofString());
        assertEquals(estado, respuesta.statusCode(), respuesta::body);
        return JSON.readTree(respuesta.body());
    }
}
