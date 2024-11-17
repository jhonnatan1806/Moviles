package com.example.pc3moviles

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException

class HuggingFaceApiHelper {
    companion object {
        private const val BASE_URL = "https://jhonnatan1806-api-ep.hf.space/predict/"

        fun enviarImagenAlServidor(archivo: File, callback: (String?) -> Unit) {
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", archivo.name,
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), archivo)
                )
                .build()

            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HuggingFaceApiHelper", "Error al enviar la imagen: ${e.message}")
                    callback(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseBody ->
                            Log.d("HuggingFaceApiHelper", "Respuesta del servidor: $responseBody")
                            // Parsear la respuesta
                            val resultado = Regex("\"prediction\":\\s*\"(.*?)\"")
                                .find(responseBody)?.groupValues?.get(1)
                            callback(resultado)
                        } ?: callback(null)
                    } else {
                        Log.e("HuggingFaceApiHelper", "Error: ${response.code} - ${response.message}")
                        callback(null)
                    }
                }
            })
        }
    }
}
