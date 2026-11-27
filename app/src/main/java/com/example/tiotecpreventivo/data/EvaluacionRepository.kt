package com.example.tiotecpreventivo.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.tiotecpreventivo.data.remote.CambioRespuesta
import com.example.tiotecpreventivo.data.remote.ChecklistRetrofit
import com.example.tiotecpreventivo.data.remote.EvaluacionApiService
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PreguntaEvaluacion(
    val id: Int,
    val texto: String,
    val respuesta: Boolean?
)

class EvaluacionRepository(
    private val api: EvaluacionApiService,
    private val obtenerPerfil: suspend () -> String
) {
    suspend fun estado() = api.evaluacion(obtenerPerfil())

    suspend fun cargar(): List<PreguntaEvaluacion> {
        val evaluacion = estado()
        val preguntas = api.preguntas()
        // Los IDs del 1 al 4 corresponden a las recomendaciones fijas del checklist.
        if (preguntas.map { it.id }.sorted() != listOf(1, 2, 3, 4)) {
            throw IOException("El catálogo de evaluación no coincide con el checklist.")
        }
        val respuestas = evaluacion.respuestas.associate { it.preguntaId to it.respuesta }
        return preguntas.sortedBy { it.orden }.map {
            PreguntaEvaluacion(it.id, it.texto, respuestas[it.id])
        }
    }

    suspend fun guardar(id: Int, respuesta: Boolean) =
        api.guardar(obtenerPerfil(), id, CambioRespuesta(respuesta))

    companion object {
        fun remoto(contexto: Context) = EvaluacionRepository(ChecklistRetrofit.evaluacion) {
            val perfil = withContext(Dispatchers.IO) { PerfilInstalacion.obtener(contexto) }
            if (Build.VERSION.SDK_INT >= 37 &&
                ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_LOCAL_NETWORK) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("Permiso de red local pendiente.")
            }
            perfil
        }
    }
}
