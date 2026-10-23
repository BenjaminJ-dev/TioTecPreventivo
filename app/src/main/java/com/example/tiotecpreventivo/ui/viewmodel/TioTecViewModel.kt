package com.example.tiotecpreventivo.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotecpreventivo.data.AccionPreventiva
import com.example.tiotecpreventivo.data.AccionPreventivaRepository
import com.example.tiotecpreventivo.data.DatabaseProvider
import kotlinx.coroutines.launch

class TioTecViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider
        .obtenerBaseDeDatos(application)
        .accionPreventivaDao()

    private val repository = AccionPreventivaRepository(dao)

    val respuestas = mutableStateListOf<Boolean?>(null, null, null, null)

    private val listaAcciones = mutableStateListOf(false, false, false, false)
    val accionesCompletadas: List<Boolean> = listaAcciones

    var mostrarError by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.obtenerTodas().forEach { accion ->
                if (accion.id in listaAcciones.indices) {
                    listaAcciones[accion.id] = accion.completada
                }
            }
        }
    }

    fun cambiarRespuesta(indice: Int, respuesta: Boolean) {
        if (indice !in respuestas.indices) return

        respuestas[indice] = respuesta

        if (respuestas.all { it != null }) {
            mostrarError = false
        }
    }

    fun validarRespuestas(): Boolean {
        val estanCompletas = respuestas.all { it != null }
        mostrarError = !estanCompletas
        return estanCompletas
    }

    fun cambiarAccion(indice: Int, marcada: Boolean) {
        if (indice !in listaAcciones.indices) return

        listaAcciones[indice] = marcada

        viewModelScope.launch {
            repository.guardar(
                AccionPreventiva(
                    id = indice,
                    completada = marcada
                )
            )
        }
    }
}