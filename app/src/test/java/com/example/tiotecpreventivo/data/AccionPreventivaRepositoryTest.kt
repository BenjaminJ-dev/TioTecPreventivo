package com.example.tiotecpreventivo.data

import com.example.tiotecpreventivo.data.remote.AccionChecklistRemota
import com.example.tiotecpreventivo.data.remote.CambioChecklistRemoto
import com.example.tiotecpreventivo.data.remote.ChecklistApiService
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class AccionPreventivaRepositoryTest {
    private val dao = DaoEnMemoria()
    private val api = ApiFake()
    private val repositorio = AccionPreventivaRepository(dao, { true }, api)

    @Test
    fun putExitosoGuardaLocalmenteYConfirmaElCambio() = runTest {
        val accion = AccionPreventiva(1, true)
        api.alActualizar = {
            assertEquals(accion, dao.obtener(1))
            Response.success(Unit)
        }

        repositorio.guardar(accion)

        assertEquals(listOf("PUT 1 true"), api.llamadas)
        assertEquals(accion.copy(pendienteDeSincronizar = false), dao.obtener(1))
        assertTrue(dao.obtenerPendientes().isEmpty())
    }

    @Test
    fun put404CreaLaAccionConPost() = runTest {
        api.alActualizar = { Response.error(404, "".toResponseBody()) }

        repositorio.guardar(AccionPreventiva(2, false))

        assertEquals(listOf("PUT 2 false", "POST 2 false"), api.llamadas)
        assertEquals(AccionPreventiva(2, false, pendienteDeSincronizar = false), dao.obtener(2))
    }

    @Test
    fun errorDeRedDejaElCambioLocalPendiente() = runTest {
        api.alActualizar = { throw IOException("Sin conexión") }
        val accion = AccionPreventiva(3, true)

        repositorio.guardar(accion)

        assertEquals(listOf("PUT 3 true"), api.llamadas)
        assertEquals(listOf(accion), dao.obtenerPendientes())
    }

    @Test
    fun getConservaElCambioPendienteYLoEnviaConSuValorLocal() = runTest {
        dao.guardar(AccionPreventiva(0, true))
        api.accionesRemotas = listOf(AccionChecklistRemota(0, false), AccionChecklistRemota(1, true))
        api.alActualizar = {
            assertEquals(AccionPreventiva(0, true), dao.obtener(0))
            Response.success(Unit)
        }

        val acciones = repositorio.actualizarDesdeServidor()

        assertEquals(listOf("GET", "PUT 0 true"), api.llamadas)
        assertEquals(listOf(
            AccionPreventiva(0, true, pendienteDeSincronizar = false),
            AccionPreventiva(1, true, pendienteDeSincronizar = false)
        ), acciones)
    }

    @Test
    fun reinicioLimpiaSoloLosIdsSolicitadosTras204Y404() = runTest {
        dao.guardar(AccionPreventiva(0, true, pendienteDeSincronizar = false))
        dao.guardar(AccionPreventiva(1, true))
        val otraAccion = AccionPreventiva(2, false)
        dao.guardar(otraAccion)
        api.alEliminar = { id ->
            assertEquals(3, dao.obtenerTodas().size)
            if (id == 0) Response.success(204, Unit)
            else Response.error(404, "".toResponseBody())
        }

        assertTrue(repositorio.reiniciarProgreso(listOf(0, 1)))

        assertEquals(listOf("DELETE 0", "DELETE 1"), api.llamadas)
        assertEquals(listOf(otraAccion), dao.obtenerTodas())
    }

    @Test
    fun reinicioConFalloParcialConservaTodosLosDatosLocales() = runTest {
        val acciones = listOf(
            AccionPreventiva(0, true, pendienteDeSincronizar = false),
            AccionPreventiva(1, true)
        )
        acciones.forEach { dao.guardar(it) }
        api.alEliminar = { id ->
            if (id == 0) Response.success(204, Unit) else throw IOException("Sin conexión")
        }

        assertFalse(repositorio.reiniciarProgreso(listOf(0, 1)))

        assertEquals(listOf("DELETE 0", "DELETE 1"), api.llamadas)
        assertEquals(acciones, dao.obtenerTodas())
    }

    internal class DaoEnMemoria : AccionPreventivaDao {
        private val filas = sortedMapOf<Int, AccionPreventiva>()

        override suspend fun obtenerTodas() = filas.values.toList()

        override suspend fun guardar(accion: AccionPreventiva) {
            filas[accion.id] = accion
        }

        override suspend fun obtenerPendientes() = filas.values.filter { it.pendienteDeSincronizar }

        override suspend fun obtener(id: Int) = filas[id]

        override suspend fun eliminarPorIds(ids: List<Int>) {
            filas.keys.removeAll(ids.toSet())
        }

        override suspend fun confirmarSincronizacion(id: Int, completada: Boolean) {
            val accion = filas[id]
            if (accion != null && accion.completada == completada) {
                filas[id] = accion.copy(pendienteDeSincronizar = false)
            }
        }
    }

    internal class ApiFake : ChecklistApiService {
        val llamadas = mutableListOf<String>()
        var accionesRemotas = emptyList<AccionChecklistRemota>()
        var alActualizar: suspend () -> Response<Unit> = { Response.success(Unit) }
        var alEliminar: suspend (Int) -> Response<Unit> = { error("DELETE inesperado") }

        override suspend fun obtenerTodas(): Response<List<AccionChecklistRemota>> {
            llamadas.add("GET")
            return Response.success(accionesRemotas)
        }

        override suspend fun crear(accion: AccionChecklistRemota): Response<Unit> {
            llamadas.add("POST ${accion.id} ${accion.completada}")
            return Response.success(201, Unit)
        }

        override suspend fun actualizar(id: Int, cambio: CambioChecklistRemoto): Response<Unit> {
            llamadas.add("PUT $id ${cambio.completada}")
            return alActualizar()
        }

        override suspend fun eliminar(id: Int): Response<Unit> {
            llamadas.add("DELETE $id")
            return alEliminar(id)
        }
    }
}
