package com.example.tiotecpreventivo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AnciRetrofit {

    private const val BASE_URL = "https://anci.gob.cl/"

    val api: AnciApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnciApiService::class.java)
    }
}
