package com.example.tiotecpreventivo.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface EvaluacionApiService {
    @GET("api/v1/preguntas")
    suspend fun preguntas(): List<PreguntaRemota>

    @GET("api/v1/evaluaciones/{perfilId}")
    suspend fun evaluacion(@Path("perfilId") perfilId: String): EvaluacionRemota

    @PUT("api/v1/evaluaciones/{perfilId}/respuestas/{preguntaId}")
    suspend fun guardar(
        @Path("perfilId") perfilId: String,
        @Path("preguntaId") preguntaId: Int,
        @Body cambio: CambioRespuesta
    ): RespuestaRemota
}

data class PreguntaRemota(
    @SerializedName("id") val id: Int,
    @SerializedName("texto") val texto: String,
    @SerializedName("orden") val orden: Int
)

data class EvaluacionRemota(
    @SerializedName("respuestas") val respuestas: List<RespuestaRemota>,
    @SerializedName("preguntasPendientes") val preguntasPendientes: List<Int>
)

data class RespuestaRemota(
    @SerializedName("preguntaId") val preguntaId: Int,
    @SerializedName("respuesta") val respuesta: Boolean
)

data class CambioRespuesta(@SerializedName("respuesta") val respuesta: Boolean)
