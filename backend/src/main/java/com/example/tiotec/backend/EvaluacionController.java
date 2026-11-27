package com.example.tiotec.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class EvaluacionController {
    private static final String FORMATO_UUID =
            "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private final JdbcClient jdbc;

    public EvaluacionController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/preguntas")
    public List<Pregunta> listarPreguntas() {
        return jdbc.sql("SELECT id, texto, orden FROM preguntas ORDER BY orden")
                .query(Pregunta.class)
                .list();
    }

    @PutMapping("/evaluaciones/{perfilId}/respuestas/{preguntaId}")
    public Respuesta guardarRespuesta(
            @PathVariable @Pattern(regexp = FORMATO_UUID) String perfilId,
            @PathVariable int preguntaId,
            @RequestBody JsonNode cuerpo) {
        JsonNode valor = cuerpo.path("respuesta");
        if (!valor.isBoolean()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La respuesta debe ser true o false.");
        }

        boolean respuesta = valor.booleanValue();
        int filas = jdbc.sql("""
                MERGE INTO respuestas_evaluacion (perfil_id, pregunta_id, respuesta)
                KEY (perfil_id, pregunta_id)
                SELECT ?, id, ? FROM preguntas WHERE id = ?
                """)
                .params(UUID.fromString(perfilId), respuesta, preguntaId)
                .update();
        if (filas == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pregunta no existe.");
        }
        return new Respuesta(preguntaId, respuesta);
    }

    @GetMapping("/evaluaciones/{perfilId}")
    public Evaluacion consultarEvaluacion(@PathVariable @Pattern(regexp = FORMATO_UUID) String perfilId) {
        UUID perfil = UUID.fromString(perfilId);
        List<Respuesta> respuestas = new ArrayList<>();
        List<Integer> pendientes = new ArrayList<>();

        jdbc.sql("""
                SELECT p.id, r.respuesta
                FROM preguntas p
                LEFT JOIN respuestas_evaluacion r ON r.pregunta_id = p.id AND r.perfil_id = ?
                ORDER BY p.orden
                """)
                .param(perfil)
                .query((rs, fila) -> new EstadoPregunta(rs.getInt("id"), rs.getObject("respuesta", Boolean.class)))
                .list()
                .forEach(estado -> {
                    if (estado.respuesta() == null) {
                        pendientes.add(estado.preguntaId());
                    } else {
                        respuestas.add(new Respuesta(estado.preguntaId(), estado.respuesta()));
                    }
                });
        return new Evaluacion(perfil, respuestas, pendientes);
    }

    public record Pregunta(int id, String texto, int orden) {}

    public record Respuesta(int preguntaId, boolean respuesta) {}

    public record Evaluacion(UUID perfilId, List<Respuesta> respuestas, List<Integer> preguntasPendientes) {}

    private record EstadoPregunta(int preguntaId, Boolean respuesta) {}
}
