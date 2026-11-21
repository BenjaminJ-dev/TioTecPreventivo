package com.example.tiotecpreventivo.data.remote

import com.example.tiotecpreventivo.data.PaginaAlertasAnci

class AlertasAnciRepository(
    private val api: AnciApiService = AnciRetrofit.api
) {
    suspend fun obtenerAlertas(): PaginaAlertasAnci {
        return api.obtenerAlertas()
    }
}
