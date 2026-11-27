package com.example.tiotecpreventivo.data.remote

import com.example.tiotecpreventivo.data.PaginaAlertasAnci
import retrofit2.http.GET

interface AnciApiService {

    @GET("api/v1/alerts/?page=1&page_size=5")
    suspend fun obtenerAlertas(): PaginaAlertasAnci
}
