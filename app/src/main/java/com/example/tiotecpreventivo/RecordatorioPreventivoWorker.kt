package com.example.tiotecpreventivo

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.tiotecpreventivo.data.remote.AccionChecklistRemota
import com.example.tiotecpreventivo.data.remote.ChecklistRetrofit
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class RecordatorioPreventivoWorker(contexto: Context, parametros: WorkerParameters) :
    CoroutineWorker(contexto, parametros) {
    override suspend fun doWork(): Result {
        val recomendaciones = inputData.getIntArray(RECOMENDACIONES) ?: return Result.success()
        if (recomendaciones.isEmpty()) return Result.success()
        if (!NotificacionHelper.puedeMostrar(applicationContext)) return Result.success()

        return try {
            val respuesta = ChecklistRetrofit.api.obtenerTodas()
            val acciones = respuesta.body() ?: return Result.failure()
            currentCoroutineContext().ensureActive()
            if (hayPendientes(recomendaciones, acciones)) {
                NotificacionHelper.mostrar(applicationContext)
            }
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        } catch (_: IOException) {
            Result.failure()
        }
    }

    companion object {
        private const val NOMBRE = "recordatorio_recomendaciones"
        private const val RECOMENDACIONES = "recomendaciones"

        fun cancelarPeriodicoAnterior(contexto: Context) {
            WorkManager.getInstance(contexto).cancelUniqueWork("recordatorio_preventivo_diario")
        }

        fun programar(contexto: Context, recomendaciones: IntArray) {
            val trabajo = OneTimeWorkRequestBuilder<RecordatorioPreventivoWorker>()
                .setInitialDelay(30, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(workDataOf(RECOMENDACIONES to recomendaciones))
                .build()
            WorkManager.getInstance(contexto).enqueueUniqueWork(
                NOMBRE, ExistingWorkPolicy.REPLACE, trabajo
            )
        }

        fun cancelar(contexto: Context) {
            WorkManager.getInstance(contexto).cancelUniqueWork(NOMBRE)
        }

        internal fun hayPendientes(recomendaciones: IntArray, acciones: List<AccionChecklistRemota>): Boolean {
            val completadas = acciones.filter { it.completada }.map { it.id }.toSet()
            return recomendaciones.any { it !in completadas }
        }
    }
}
