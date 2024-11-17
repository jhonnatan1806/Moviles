package com.example.pc3moviles

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Multipart

interface PhotoApi {
    @Multipart
    @POST("/predict/")
    fun predict(@Part file: MultipartBody.Part): Call<ResponseData>
    //fun predict(@Body request: RequestData): Call<ResponseData>
}