package com.example.tiotecpreventivo.ui.viewmodel

import com.example.tiotecpreventivo.data.AlertaAnci
import com.example.tiotecpreventivo.data.PaginaAlertasAnci
import com.example.tiotecpreventivo.data.remote.AlertasAnciRepository
import com.example.tiotecpreventivo.data.remote.AnciApiService
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class AlertasAnciViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun prepararDispatcher() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun restaurarDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargaExitosaConservaSoloCampanasFraudulentas() {
        val campana = alerta("Aviso de suplantación", "  campaña fraudulenta  ")
        val vulnerabilidad = alerta("Actualización técnica", "Vulnerabilidad Crítica")
        val sinTipo = alerta("Aviso sin clasificación", null)
        val pagina = PaginaAlertasAnci(listOf(vulnerabilidad, campana, sinTipo), 3)
        val api = ApiAnciFake(listOf(Result.success(pagina)))
        val modelo = AlertasAnciViewModel(AlertasAnciRepository(api))

        assertEquals(EstadoAlertasAnci.Cargando, modelo.estado)
        dispatcher.scheduler.runCurrent()

        assertEquals(EstadoAlertasAnci.Recibidas(listOf(campana)), modelo.estado)
        assertEquals(1, api.llamadas)
    }

    @Test
    fun respuestaVaciaQuedaComoRecibidasSinAlertas() {
        val api = ApiAnciFake(listOf(Result.success(PaginaAlertasAnci(emptyList(), 0))))
        val modelo = AlertasAnciViewModel(AlertasAnciRepository(api))

        dispatcher.scheduler.runCurrent()

        assertEquals(EstadoAlertasAnci.Recibidas(emptyList()), modelo.estado)
        assertEquals(1, api.llamadas)
    }

    @Test
    fun trasUnErrorReintentarPuedeCargarLasAlertas() {
        val campana = alerta("Aviso de fraude", "Campaña Fraudulenta")
        val api = ApiAnciFake(listOf(
            Result.failure(IOException("Sin conexión")),
            Result.success(PaginaAlertasAnci(listOf(campana), 1))
        ))
        val modelo = AlertasAnciViewModel(AlertasAnciRepository(api))
        dispatcher.scheduler.runCurrent()
        assertTrue(modelo.estado is EstadoAlertasAnci.Error)
        assertEquals(1, api.llamadas)

        modelo.reintentar()
        assertEquals(EstadoAlertasAnci.Cargando, modelo.estado)
        dispatcher.scheduler.runCurrent()

        assertEquals(EstadoAlertasAnci.Recibidas(listOf(campana)), modelo.estado)
        assertEquals(2, api.llamadas)
    }

    @Test
    fun reintentarDuranteLaCargaOTrasElExitoNoDuplicaLaLlamada() {
        val pagina = PaginaAlertasAnci(emptyList(), 0)
        val api = ApiAnciFake(listOf(Result.success(pagina)))
        val modelo = AlertasAnciViewModel(AlertasAnciRepository(api))

        modelo.reintentar()
        dispatcher.scheduler.runCurrent()
        assertEquals(1, api.llamadas)
        assertEquals(EstadoAlertasAnci.Recibidas(emptyList()), modelo.estado)

        modelo.reintentar()
        dispatcher.scheduler.runCurrent()

        assertEquals(1, api.llamadas)
        assertEquals(EstadoAlertasAnci.Recibidas(emptyList()), modelo.estado)
    }

    private fun alerta(titulo: String, tipo: String?) = AlertaAnci(
        titulo = titulo,
        codigo = null,
        categoria = "alertas",
        tipoIncidente = tipo,
        fecha = null,
        descripcionGeneral = null,
        mitigacion = null,
        enlacesRelacionados = null,
        entidadesAfectadas = null,
        descripcionEspecifica = null
    )

    private class ApiAnciFake(respuestas: List<Result<PaginaAlertasAnci>>) : AnciApiService {
        private val pendientes = ArrayDeque(respuestas)
        var llamadas = 0
            private set

        override suspend fun obtenerAlertas(): PaginaAlertasAnci {
            llamadas++
            return pendientes.removeFirst().getOrThrow()
        }
    }
}
