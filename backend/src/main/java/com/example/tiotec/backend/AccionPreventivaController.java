package com.example.tiotec.backend;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/acciones-preventivas")
public class AccionPreventivaController {
    private final JdbcClient jdbc;

    public AccionPreventivaController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping
    public ResponseEntity<AccionPreventiva> crear(@Valid @RequestBody AccionPreventiva accion) {
        try {
            jdbc.sql("INSERT INTO acciones_preventivas (id, completada) VALUES (?, ?)")
                    .params(accion.id(), accion.completada())
                    .update();
        } catch (DuplicateKeyException error) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La acción ya existe.");
        }

        URI ubicacion = URI.create("/api/v1/acciones-preventivas/" + accion.id());
        return ResponseEntity.created(ubicacion).body(accion);
    }

    @GetMapping
    public List<AccionPreventiva> listar() {
        return jdbc.sql("SELECT id, completada FROM acciones_preventivas ORDER BY id")
                .query(AccionPreventiva.class)
                .list();
    }

    @GetMapping("/{id}")
    public AccionPreventiva consultar(@PathVariable int id) {
        return jdbc.sql("SELECT id, completada FROM acciones_preventivas WHERE id = ?")
                .param(id)
                .query(AccionPreventiva.class)
                .optional()
                .orElseThrow(AccionPreventivaController::accionNoEncontrada);
    }

    @PutMapping("/{id}")
    public AccionPreventiva actualizar(@PathVariable int id, @Valid @RequestBody Actualizacion cambio) {
        int filas = jdbc.sql("UPDATE acciones_preventivas SET completada = ? WHERE id = ?")
                .params(cambio.completada(), id)
                .update();
        if (filas == 0) {
            throw accionNoEncontrada();
        }
        return new AccionPreventiva(id, cambio.completada());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        int filas = jdbc.sql("DELETE FROM acciones_preventivas WHERE id = ?")
                .param(id)
                .update();
        if (filas == 0) {
            throw accionNoEncontrada();
        }
        return ResponseEntity.noContent().build();
    }

    private static ResponseStatusException accionNoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "La acción no existe.");
    }

    public record Actualizacion(@NotNull Boolean completada) {}
}
