package com.example.pc3moviles

import okhttp3.MultipartBody

data class RequestData(val file: MultipartBody.Part)

data class ResponseData(
    val prediction: String
)