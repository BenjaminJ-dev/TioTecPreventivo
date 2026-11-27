package com.example.tiotecpreventivo.ui.viewmodel

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.tiotecpreventivo.data.EvaluacionRepository
import com.example.tiotecpreventivo.data.PerfilInstalacion
import com.example.tiotecpreventivo.data.remote.CambioRespuesta
import com.example.tiotecpreventivo.data.remote.EvaluacionApiService
import com.example.tiotecpreventivo.data.remote.EvaluacionRemota
import com.example.tiotecpreventivo.data.remote.PreguntaRemota
import com.example.tiotecpreventivo.data.remote.RespuestaRemota
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class EvaluacionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val app get() = RuntimeEnvironment.getApplication()
    private val perfil = UUID.randomUUID().toString()
    private val api = ApiEnMemoria()
    private lateinit var modelo: EvaluacionViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                modelClass.cast(EvaluacionViewModel(app, EvaluacionRepository(api) { perfil }))!!
        }
        modelo = ViewModelProvider(store, factory)[EvaluacionViewModel::class.java]
    }

    @After
    fun terminar() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun cargaPreguntasOrdenadasYRespuestasPorIdIncluidoNo() {
        api.respuestas[4] = false
        api.respuestas[2] = true
        modelo.cargar()
        assertTrue(modelo.sincronizando)
        dispatcher.scheduler.runCurrent()

        assertEquals(listOf(1, 2, 3, 4), modelo.preguntas.map { it.id })
        assertEquals(listOf("Pregunta 1", "Pregunta 2", "Pregunta 3", "Pregunta 4"), modelo.preguntas.map { it.texto })
        assertEquals(listOf(null, true, null, false), modelo.preguntas.map { it.respuesta })
        assertEquals(listOf(perfil), api.perfiles)
        assertFalse(modelo.validarRespuestas())
        assertTrue(modelo.mostrarError)
    }

    @Test
    fun muestraSiYNoSoloCuandoElServidorLosConfirma() {
        modelo.cargar()
        dispatcher.scheduler.runCurrent()

        modelo.cambiarRespuesta(1, true)
        assertNull(modelo.preguntas.first().respuesta)
        dispatcher.scheduler.runCurrent()
        assertEquals(true, modelo.preguntas.first().respuesta)
        modelo.cambiarRespuesta(1, false)
        assertEquals(true, modelo.preguntas.first().respuesta)
        dispatcher.scheduler.runCurrent()
        assertEquals(false, modelo.preguntas.first().respuesta)
        assertEquals(listOf(Triple(perfil, 1, true), Triple(perfil, 1, false)), api.envios)
    }

    @Test
    fun unPutFallidoConservaLaRespuestaConfirmadaYReintentarSoloLeeElBackend() {
        api.respuestas[2] = true
        modelo.cargar()
        dispatcher.scheduler.runCurrent()
        api.sinRed = true
        modelo.cambiarRespuesta(2, false)
        dispatcher.scheduler.runCurrent()

        assertNotNull(modelo.errorRed)
        assertEquals(true, modelo.preguntas.single { it.id == 2 }.respuesta)
        assertFalse(modelo.validarRespuestas())

        api.sinRed = false
        modelo.cargar()
        dispatcher.scheduler.runCurrent()
        assertNull(modelo.errorRed)
        assertEquals(true, api.respuestas[2])
        assertEquals(listOf(Triple(perfil, 2, false)), api.envios)
    }

    @Test
    fun permiteReintentarLaCargaYExigeTodasLasRespuestasParaElResultado() {
        api.sinRed = true
        modelo.cargar()
        dispatcher.scheduler.runCurrent()
        assertNotNull(modelo.errorRed)
        assertTrue(modelo.preguntas.isEmpty())
        assertFalse(modelo.validarRespuestas())

        api.sinRed = false
        modelo.cargar()
        dispatcher.scheduler.runCurrent()
        assertNull(modelo.errorRed)
        modelo.cambiarRespuesta(99, false)
        for (id in 1..3) {
            modelo.cambiarRespuesta(id, true)
            dispatcher.scheduler.runCurrent()
        }
        assertFalse(modelo.validarRespuestas())
        modelo.cambiarRespuesta(4, false)
        dispatcher.scheduler.runCurrent()
        assertFalse(modelo.mostrarError)
        assertTrue(modelo.validarRespuestas())
    }

    @Test
    fun conservaSoloElUuidAnteriorEnPreferencias() {
        val archivo = app.noBackupFilesDir.resolve("tiotec_evaluacion.db")
        SQLiteDatabase.openOrCreateDatabase(archivo, null).use { base ->
            base.execSQL("CREATE TABLE instalacion (id INTEGER PRIMARY KEY, uuid TEXT)")
            base.execSQL("INSERT INTO instalacion VALUES (0, ?)", arrayOf(perfil))
        }

        assertEquals(perfil, PerfilInstalacion.obtener(app))
        assertFalse(archivo.exists())
        assertEquals(perfil, PerfilInstalacion.obtener(app))
    }

    private class ApiEnMemoria : EvaluacionApiService {
        val respuestas = mutableMapOf<Int, Boolean>()
        val envios = mutableListOf<Triple<String, Int, Boolean>>()
        val perfiles = mutableListOf<String>()
        var sinRed = false

        override suspend fun preguntas(): List<PreguntaRemota> {
            comprobarConexion()
            return (4 downTo 1).map { PreguntaRemota(it, "Pregunta $it", it) }
        }

        override suspend fun evaluacion(perfilId: String): EvaluacionRemota {
            comprobarConexion()
            perfiles.add(perfilId)
            return EvaluacionRemota(
                respuestas.map { (id, respuesta) -> RespuestaRemota(id, respuesta) },
                (1..4).filterNot(respuestas::containsKey)
            )
        }

        override suspend fun guardar(perfilId: String, preguntaId: Int, cambio: CambioRespuesta): RespuestaRemota {
            envios.add(Triple(perfilId, preguntaId, cambio.respuesta))
            comprobarConexion()
            respuestas[preguntaId] = cambio.respuesta
            return RespuestaRemota(preguntaId, cambio.respuesta)
        }

        private fun comprobarConexion() {
            if (sinRed) throw IOException("Sin conexión")
        }
    }
}
