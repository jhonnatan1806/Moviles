package com.example.pc3moviles

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitPhoto {
    private const val BASE_URL = "https://jhonnatan1806-api-ep.hf.space"

    val instance: PhotoApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PhotoApi::class.java)
    }
}