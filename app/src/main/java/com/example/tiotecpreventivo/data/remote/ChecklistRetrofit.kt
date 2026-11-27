package com.example.tiotecpreventivo.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChecklistRetrofit {
    private val retrofit: Retrofit by lazy {
        val cliente = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ChecklistApiService by lazy { retrofit.create(ChecklistApiService::class.java) }
    val evaluacion: EvaluacionApiService by lazy { retrofit.create(EvaluacionApiService::class.java) }
}
