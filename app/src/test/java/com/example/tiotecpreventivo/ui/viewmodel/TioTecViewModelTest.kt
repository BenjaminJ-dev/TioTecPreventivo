package com.example.tiotecpreventivo.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.tiotecpreventivo.data.AccionPreventiva
import com.example.tiotecpreventivo.data.AccionPreventivaRepository
import com.example.tiotecpreventivo.data.AccionPreventivaRepositoryTest.ApiFake
import com.example.tiotecpreventivo.data.AccionPreventivaRepositoryTest.DaoEnMemoria
import com.example.tiotecpreventivo.data.remote.AccionChecklistRemota
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class TioTecViewModelTest {
    private val viewModelStore = ViewModelStore()
    private val dispatcher = StandardTestDispatcher()
    private val dao = DaoEnMemoria()
    private val api = ApiFake()
    private lateinit var modelo: TioTecViewModel

    @Before
    fun prepararModelo() {
        Dispatchers.setMain(dispatcher)
        val factory = ViewModelProvider.AndroidViewModelFactory(RuntimeEnvironment.getApplication())
        modelo = ViewModelProvider(viewModelStore, factory)[TioTecViewModel::class.java]
        // Sustituye el repositorio antes de ejecutar la carga inicial encolada.
        val campo = TioTecViewModel::class.java.getDeclaredField("repository")
        campo.isAccessible = true
        campo.set(modelo, AccionPreventivaRepository(dao, { true }, api))
    }

    @After
    fun limpiarModelo() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun unaRespuestaPendienteImpideValidarYActivaElError() {
        responderPrimerasTres()

        assertFalse(modelo.validarRespuestas())
        assertTrue(modelo.mostrarError)
    }

    @Test
    fun cuatroRespuestasContestadasPermitenValidar() {
        responderPrimerasTres()
        modelo.cambiarRespuesta(3, false)

        assertTrue(modelo.validarRespuestas())
    }

    @Test
    fun completarLaRespuestaPendienteLimpiaElErrorSinVolverAValidar() {
        responderPrimerasTres()
        assertFalse(modelo.validarRespuestas())
        assertTrue(modelo.mostrarError)

        modelo.cambiarRespuesta(3, false)

        assertFalse(modelo.mostrarError)
    }

    @Test
    fun cambiarCasillaGuardaSuIndiceYAmbosValores() = runBlocking {
        dispatcher.scheduler.runCurrent()

        modelo.cambiarAccion(2, true)
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(false, false, true, false), modelo.accionesCompletadas)
        assertEquals(AccionPreventiva(2, true, false), dao.obtener(2))

        modelo.cambiarAccion(2, false)
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(false, false, false, false), modelo.accionesCompletadas)
        assertEquals(AccionPreventiva(2, false, false), dao.obtener(2))
        assertEquals(listOf("GET", "PUT 2 true", "PUT 2 false"), api.llamadas)
    }

    @Test
    fun cargarChecklistCombinaServidorYRoomSinPisarUnaMarcaPendiente() = runBlocking {
        val pendiente = AccionPreventiva(0, true)
        dao.guardar(pendiente)
        api.accionesRemotas = listOf(
            AccionChecklistRemota(0, false),
            AccionChecklistRemota(1, true),
            AccionChecklistRemota(7, true)
        )
        api.alActualizar = { throw IOException("Sin conexión") }

        dispatcher.scheduler.runCurrent()

        assertEquals(listOf(true, true, false, false), modelo.accionesCompletadas)
        assertEquals(pendiente, dao.obtener(0))
        assertEquals(listOf("GET", "PUT 0 true"), api.llamadas)
    }

    @Test
    fun sincronizarConUnaEdicionEnColaConservaLaMarcaDePantalla() {
        dispatcher.scheduler.runCurrent()
        api.accionesRemotas = listOf(AccionChecklistRemota(2, false))

        modelo.sincronizarChecklist()
        modelo.cambiarAccion(2, true)
        dispatcher.scheduler.runCurrent()

        assertTrue(modelo.accionesCompletadas[2])
        assertEquals(listOf("GET", "GET", "PUT 2 true"), api.llamadas)
    }

    @Test
    fun reinicioExitosoEsperaTodasLasEliminacionesEIgnoraIntentosRepetidos() = runBlocking {
        dao.guardar(AccionPreventiva(1, true, false))
        modelo.cambiarRespuesta(0, true)
        dispatcher.scheduler.runCurrent()
        val ultimaEliminacion = CompletableDeferred<Unit>()
        api.alEliminar = { id ->
            if (id == 3) ultimaEliminacion.await()
            Response.success(204, Unit)
        }

        modelo.reiniciarProgreso()
        dispatcher.scheduler.runCurrent()
        assertTrue(modelo.reiniciandoProgreso)
        assertTrue(modelo.accionesCompletadas[1])
        assertNotNull(dao.obtener(1))

        modelo.reiniciarProgreso()
        modelo.cambiarAccion(0, true)
        assertFalse(modelo.accionesCompletadas[0])
        ultimaEliminacion.complete(Unit)
        dispatcher.scheduler.runCurrent()

        assertEquals(listOf("GET", "DELETE 0", "DELETE 1", "DELETE 2", "DELETE 3"), api.llamadas)
        assertEquals(listOf(false, false, false, false), modelo.accionesCompletadas)
        assertTrue(dao.obtenerTodas().isEmpty())
        assertEquals(true, modelo.respuestas[0])
        assertFalse(modelo.reiniciandoProgreso)
        assertNull(modelo.errorReinicio)
    }

    @Test
    fun reinicioFallidoConservaLasMarcasYExponeElError() = runBlocking {
        val guardada = AccionPreventiva(1, true, false)
        dao.guardar(guardada)
        dispatcher.scheduler.runCurrent()
        api.alEliminar = { id ->
            if (id == 1) throw IOException("Sin conexión")
            Response.success(204, Unit)
        }

        modelo.reiniciarProgreso()
        dispatcher.scheduler.runCurrent()

        assertEquals(listOf("GET", "DELETE 0", "DELETE 1"), api.llamadas)
        assertEquals(listOf(false, true, false, false), modelo.accionesCompletadas)
        assertEquals(listOf(guardada), dao.obtenerTodas())
        assertFalse(modelo.reiniciandoProgreso)
        assertNotNull(modelo.errorReinicio)
    }

    @Test
    fun indicesInvalidosNoCambianRespuestasNiGuardanAcciones() = runBlocking {
        dispatcher.scheduler.runCurrent()

        for (indice in listOf(-1, 4)) {
            modelo.cambiarRespuesta(indice, true)
            modelo.cambiarAccion(indice, true)
        }
        dispatcher.scheduler.runCurrent()

        assertEquals(listOf(null, null, null, null), modelo.respuestas)
        assertEquals(listOf(false, false, false, false), modelo.accionesCompletadas)
        assertTrue(dao.obtenerTodas().isEmpty())
        assertEquals(listOf("GET"), api.llamadas)
    }

    private fun responderPrimerasTres() {
        modelo.cambiarRespuesta(0, true)
        modelo.cambiarRespuesta(1, false)
        modelo.cambiarRespuesta(2, true)
    }
}
