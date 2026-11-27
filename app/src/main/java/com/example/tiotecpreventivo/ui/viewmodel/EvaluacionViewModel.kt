package com.example.tiotecpreventivo.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotecpreventivo.data.EvaluacionRepository
import com.example.tiotecpreventivo.data.PreguntaEvaluacion
import java.io.IOException
import kotlinx.coroutines.launch
import retrofit2.HttpException

class EvaluacionViewModel(
    application: Application,
    private val repository: EvaluacionRepository
) : AndroidViewModel(application) {
    constructor(application: Application) : this(application, EvaluacionRepository.remoto(application))

    var preguntas by mutableStateOf<List<PreguntaEvaluacion>>(emptyList())
        private set
    var sincronizando by mutableStateOf(false)
        private set
    var errorRed by mutableStateOf<String?>(null)
        private set
    var mostrarError by mutableStateOf(false)
        private set

    fun cargar() {
        consultar("No se pudo cargar la evaluación. Reintenta para consultar el estado del servidor.") {
            preguntas = repository.cargar()
            if (estanCompletas()) mostrarError = false
        }
    }

    fun cambiarRespuesta(id: Int, respuesta: Boolean) {
        if (errorRed != null || preguntas.none { it.id == id }) return

        consultar("No se pudo confirmar la respuesta. Reintenta la consulta y vuelve a marcarla si hace falta.") {
            val guardada = repository.guardar(id, respuesta)
            if (guardada.preguntaId != id) throw IOException("El servidor confirmó otra pregunta.")
            preguntas = preguntas.map {
                if (it.id == id) it.copy(respuesta = guardada.respuesta) else it
            }
            if (estanCompletas()) mostrarError = false
        }
    }

    fun validarRespuestas(): Boolean {
        if (sincronizando || errorRed != null) return false
        return estanCompletas().also { mostrarError = !it }
    }

    private fun estanCompletas(): Boolean = preguntas.isNotEmpty() && preguntas.all { it.respuesta != null }

    private fun consultar(mensajeError: String, operacion: suspend () -> Unit) {
        if (sincronizando) return
        sincronizando = true
        errorRed = null
        viewModelScope.launch {
            try {
                operacion()
            } catch (_: IOException) {
                errorRed = mensajeError
            } catch (_: HttpException) {
                errorRed = mensajeError
            } catch (_: SecurityException) {
                errorRed = "Permite el acceso a la red local para conectar con el servidor y pulsa Reintentar."
            } finally {
                sincronizando = false
            }
        }
    }
}
