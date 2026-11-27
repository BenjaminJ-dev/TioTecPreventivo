package com.example.tiotec.backend;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockMakers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccionPreventivaControllerTest {
    @Mock(mockMaker = MockMakers.PROXY)
    private JdbcClient jdbc;
    @Mock(mockMaker = MockMakers.PROXY)
    private JdbcClient.StatementSpec sentencia;
    @Mock(mockMaker = MockMakers.PROXY)
    private JdbcClient.MappedQuerySpec<AccionPreventiva> consulta;

    private AccionPreventivaController controlador;

    @BeforeEach
    void prepararControlador() {
        controlador = new AccionPreventivaController(jdbc);
    }

    @Test
    void crearDevuelve201ConLaAccionYSuUbicacion() {
        prepararInsercion();
        when(sentencia.update()).thenReturn(1);

        var accion = new AccionPreventiva(0, false);
        var respuesta = controlador.crear(accion);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(URI.create("/api/v1/acciones-preventivas/0"), respuesta.getHeaders().getLocation());
        assertEquals(accion, respuesta.getBody());
    }

    @Test
    void crearUnIdDuplicadoDevuelve409() {
        prepararInsercion();
        when(sentencia.update()).thenThrow(new DuplicateKeyException("ID duplicado"));

        var error = assertThrows(ResponseStatusException.class,
                () -> controlador.crear(new AccionPreventiva(0, false)));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    @Test
    void listarDevuelveLasAccionesDeLaConsultaOrdenadaPorId() {
        var acciones = List.of(new AccionPreventiva(0, true), new AccionPreventiva(3, false));
        when(jdbc.sql("SELECT id, completada FROM acciones_preventivas ORDER BY id")).thenReturn(sentencia);
        when(sentencia.query(AccionPreventiva.class)).thenReturn(consulta);
        when(consulta.list()).thenReturn(acciones);

        assertEquals(acciones, controlador.listar());
    }

    @Test
    void consultarUnIdExistenteDevuelveLaAccion() {
        var accion = new AccionPreventiva(3, true);
        prepararConsultaPorId(3);
        when(consulta.optional()).thenReturn(Optional.of(accion));

        assertEquals(accion, controlador.consultar(3));
    }

    @Test
    void consultarUnIdInexistenteDevuelve404() {
        prepararConsultaPorId(9);
        when(consulta.optional()).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class, () -> controlador.consultar(9));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void actualizarUnIdExistenteDevuelveLaAccionActualizada() {
        prepararActualizacion(3, true);
        when(sentencia.update()).thenReturn(1);

        var actualizada = controlador.actualizar(3, new AccionPreventivaController.Actualizacion(true));

        assertEquals(new AccionPreventiva(3, true), actualizada);
    }

    @Test
    void actualizarUnIdInexistenteDevuelve404() {
        prepararActualizacion(9, false);
        when(sentencia.update()).thenReturn(0);

        var error = assertThrows(ResponseStatusException.class,
                () -> controlador.actualizar(9, new AccionPreventivaController.Actualizacion(false)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void eliminarUnIdExistenteDevuelve204SinCuerpo() {
        prepararEliminacion(3);
        when(sentencia.update()).thenReturn(1);

        var respuesta = controlador.eliminar(3);

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void eliminarUnIdInexistenteDevuelve404() {
        prepararEliminacion(9);
        when(sentencia.update()).thenReturn(0);

        var error = assertThrows(ResponseStatusException.class, () -> controlador.eliminar(9));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    private void prepararInsercion() {
        when(jdbc.sql("INSERT INTO acciones_preventivas (id, completada) VALUES (?, ?)")).thenReturn(sentencia);
        when(sentencia.params(0, false)).thenReturn(sentencia);
    }

    private void prepararConsultaPorId(int id) {
        when(jdbc.sql("SELECT id, completada FROM acciones_preventivas WHERE id = ?")).thenReturn(sentencia);
        when(sentencia.param(id)).thenReturn(sentencia);
        when(sentencia.query(AccionPreventiva.class)).thenReturn(consulta);
    }

    private void prepararActualizacion(int id, boolean completada) {
        when(jdbc.sql("UPDATE acciones_preventivas SET completada = ? WHERE id = ?")).thenReturn(sentencia);
        when(sentencia.params(completada, id)).thenReturn(sentencia);
    }

    private void prepararEliminacion(int id) {
        when(jdbc.sql("DELETE FROM acciones_preventivas WHERE id = ?")).thenReturn(sentencia);
        when(sentencia.param(id)).thenReturn(sentencia);
    }
}
