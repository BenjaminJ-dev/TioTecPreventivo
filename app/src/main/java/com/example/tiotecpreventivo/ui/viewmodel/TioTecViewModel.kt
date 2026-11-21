package com.example.tiotecpreventivo.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotecpreventivo.data.AccionPreventiva
import com.example.tiotecpreventivo.data.AccionPreventivaRepository
import com.example.tiotecpreventivo.data.DatabaseProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class TioTecViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider
        .obtenerBaseDeDatos(application)
        .accionPreventivaDao()

    private val repository = AccionPreventivaRepository(
        dao = dao,
        puedeAccederAlBackend = {
            Build.VERSION.SDK_INT < 37 ||
                ContextCompat.checkSelfPermission(
                    application, Manifest.permission.ACCESS_LOCAL_NETWORK
                ) == PackageManager.PERMISSION_GRANTED
        }
    )

    val respuestas = mutableStateListOf<Boolean?>(null, null, null, null)

    private val listaAcciones = mutableStateListOf(false, false, false, false)
    val accionesCompletadas: List<Boolean> = listaAcciones
    private val accionesEditadas = mutableSetOf<Int>()
    private val guardadosEnCurso = mutableSetOf<Job>()

    var reiniciandoProgreso by mutableStateOf(false)
        private set
    var errorReinicio by mutableStateOf<String?>(null)
        private set

    var mostrarError by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            mostrarAccionesGuardadas(repository.obtenerTodas())
            mostrarAccionesGuardadas(repository.actualizarDesdeServidor())
        }
    }

    fun sincronizarChecklist() {
        viewModelScope.launch {
            mostrarAccionesGuardadas(repository.actualizarDesdeServidor())
        }
    }

    private fun mostrarAccionesGuardadas(acciones: List<AccionPreventiva>) {
        for (accion in acciones) {
            if (accion.id in listaAcciones.indices && accion.id !in accionesEditadas) {
                listaAcciones[accion.id] = accion.completada
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
        if (reiniciandoProgreso || indice !in listaAcciones.indices) return

        accionesEditadas.add(indice)
        listaAcciones[indice] = marcada

        val guardado = viewModelScope.launch {
            repository.guardar(
                AccionPreventiva(
                    id = indice,
                    completada = marcada
                )
            )
        }
        guardadosEnCurso.add(guardado)
        guardado.invokeOnCompletion { guardadosEnCurso.remove(guardado) }
    }

    fun reiniciarProgreso() {
        if (reiniciandoProgreso) return

        reiniciandoProgreso = true
        errorReinicio = null
        viewModelScope.launch {
            try {
                guardadosEnCurso.toList().joinAll()
                val ids = listaAcciones.indices.toList()
                if (repository.reiniciarProgreso(ids)) {
                    for (id in ids) {
                        listaAcciones[id] = false
                    }
                    accionesEditadas.clear()
                } else {
                    errorReinicio = "No se pudo reiniciar el progreso. Se conservaron tus marcas en este dispositivo. Intenta de nuevo."
                }
            } finally {
                reiniciandoProgreso = false
            }
        }
    }
}
