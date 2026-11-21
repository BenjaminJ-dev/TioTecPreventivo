package com.example.tiotecpreventivo.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotecpreventivo.data.AlertaAnci
import com.example.tiotecpreventivo.data.remote.AlertasAnciRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed interface EstadoAlertasAnci {
    object Cargando : EstadoAlertasAnci
    data class Recibidas(val alertas: List<AlertaAnci>) : EstadoAlertasAnci
    data class Error(val mensaje: String) : EstadoAlertasAnci
}

class AlertasAnciViewModel(
    private val repositorio: AlertasAnciRepository = AlertasAnciRepository()
) : ViewModel() {

    var estado by mutableStateOf<EstadoAlertasAnci>(EstadoAlertasAnci.Cargando)
        private set

    init {
        cargarAlertas()
    }

    fun reintentar() {
        if (estado is EstadoAlertasAnci.Error) {
            cargarAlertas()
        }
    }

    private fun cargarAlertas() {
        estado = EstadoAlertasAnci.Cargando
        viewModelScope.launch {
            try {
                val pagina = repositorio.obtenerAlertas()
                val campanas = pagina.items.filter { alerta ->
                    alerta.tipoIncidente?.trim().equals("Campaña Fraudulenta", ignoreCase = true)
                }
                estado = EstadoAlertasAnci.Recibidas(campanas)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                estado = EstadoAlertasAnci.Error("No se pudieron cargar las alertas de ANCI.")
            }
        }
    }
}
