package com.example.tiotecpreventivo.data

import android.util.Log
import com.example.tiotecpreventivo.data.remote.AccionChecklistRemota
import com.example.tiotecpreventivo.data.remote.CambioChecklistRemoto
import com.example.tiotecpreventivo.data.remote.ChecklistApiService
import com.example.tiotecpreventivo.data.remote.ChecklistRetrofit
import com.google.gson.JsonParseException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import java.io.IOException

class AccionPreventivaRepository(
    private val dao: AccionPreventivaDao,
    private val puedeAccederAlBackend: () -> Boolean,
    private val api: ChecklistApiService = ChecklistRetrofit.api
) {
    private val sincronizacion = Mutex()

    suspend fun obtenerTodas(): List<AccionPreventiva> {
        return dao.obtenerTodas()
    }

    suspend fun guardar(accion: AccionPreventiva) {
        dao.guardar(accion.copy(pendienteDeSincronizar = true))
        Log.d(TAG, "Room id=${accion.id} completada=${accion.completada} pendiente=true")
        sincronizar(consultarServidor = false, idPrioritario = accion.id)
    }

    suspend fun actualizarDesdeServidor(): List<AccionPreventiva> {
        sincronizar(consultarServidor = true)
        return dao.obtenerTodas()
    }

    suspend fun reiniciarProgreso(ids: List<Int>): Boolean = sincronizacion.withLock {
        if (!puedeAccederAlBackend()) {
            Log.d(TAG, "Reinicio omitido: falta permiso de red local")
            return@withLock false
        }

        for (id in ids) {
            val codigo = solicitar("DELETE", id = id) { api.eliminar(id) }?.code()
            if (codigo != 204 && codigo != 404) return@withLock false
        }

        dao.eliminarPorIds(ids)
        true
    }

    private suspend fun sincronizar(consultarServidor: Boolean, idPrioritario: Int? = null) {
        sincronizacion.withLock {
            if (!puedeAccederAlBackend()) {
                Log.d(TAG, "Sincronización omitida id=${idPrioritario ?: "-"}: falta permiso de red local")
                return@withLock
            }

            if (consultarServidor) {
                val remotas = solicitar("GET") { api.obtenerTodas() }?.body()
                if (remotas != null) {
                    dao.guardarDesdeServidor(remotas.map { remota ->
                        AccionPreventiva(remota.id, remota.completada, pendienteDeSincronizar = false)
                    })
                }
            }

            val pendientes = dao.obtenerPendientes().sortedBy { it.id != idPrioritario }
            for (pendiente in pendientes) {
                val accion = dao.obtener(pendiente.id) ?: continue
                if (accion.pendienteDeSincronizar && enviar(accion)) {
                    dao.confirmarSincronizacion(accion.id, accion.completada)
                }
            }
        }
    }

    private suspend fun enviar(accion: AccionPreventiva): Boolean {
        val cambio = CambioChecklistRemoto(accion.completada)
        val actualizacion = solicitar("PUT", accion.id, accion.completada) {
            api.actualizar(accion.id, cambio)
        } ?: return false
        if (actualizacion.code() != 404) return actualizacion.isSuccessful

        val creacion = solicitar("POST", accion.id, accion.completada) {
            api.crear(AccionChecklistRemota(accion.id, accion.completada))
        } ?: return false
        if (creacion.code() != 409) return creacion.isSuccessful

        return solicitar("PUT", accion.id, accion.completada) { api.actualizar(accion.id, cambio) }
            ?.isSuccessful == true
    }

    private suspend fun <T> solicitar(
        metodo: String,
        id: Int? = null,
        completada: Boolean? = null,
        llamada: suspend () -> Response<T>
    ): Response<T>? {
        val detalle = buildString {
            append(metodo)
            if (id != null) append(" id=$id")
            if (completada != null) append(" completada=$completada")
        }
        return try {
            Log.d(TAG, "$detalle enviando")
            val respuesta = llamada()
            Log.d(TAG, "$detalle -> HTTP ${respuesta.code()}")
            respuesta.errorBody()?.close()
            respuesta
        } catch (error: IOException) {
            Log.w(TAG, "$detalle -> error de red", error)
            null
        } catch (error: JsonParseException) {
            Log.w(TAG, "$detalle -> error al leer JSON", error)
            null
        }
    }

    private companion object {
        const val TAG = "TioTecChecklist"
    }
}
