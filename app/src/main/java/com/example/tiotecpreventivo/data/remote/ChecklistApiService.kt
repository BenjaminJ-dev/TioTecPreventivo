package com.example.tiotecpreventivo.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChecklistApiService {
    @GET("api/v1/acciones-preventivas")
    suspend fun obtenerTodas(): Response<List<AccionChecklistRemota>>

    @POST("api/v1/acciones-preventivas")
    suspend fun crear(@Body accion: AccionChecklistRemota): Response<Unit>

    @PUT("api/v1/acciones-preventivas/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body cambio: CambioChecklistRemoto): Response<Unit>

    @DELETE("api/v1/acciones-preventivas/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>
}

data class AccionChecklistRemota(
    @SerializedName("id") val id: Int,
    @SerializedName("completada") val completada: Boolean
)

data class CambioChecklistRemoto(
    @SerializedName("completada") val completada: Boolean
)
