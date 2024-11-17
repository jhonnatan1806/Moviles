package com.example.pc3moviles

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class FirebaseStorageHelper {

    companion object {
        fun subirImagenAlStorage(uriImagen: Uri, callback: (String) -> Unit) {
            // Referencia a Firebase Storage
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference

            // Generar el nombre basado en fecha y hora
            val formatoFecha = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
            val fechaActual = Date()
            val nombreImagen = "app_${formatoFecha.format(fechaActual)}.jpg"

            // Ruta en Storage
            val imagenRef = storageRef.child("imagenes/$nombreImagen")

            // Subir la imagen
            imagenRef.putFile(uriImagen)
                .addOnSuccessListener {
                    // Obtener la URL de descarga
                    imagenRef.downloadUrl.addOnSuccessListener { uri ->
                        callback(uri.toString()) // Devolver la URL
                    }
                }
                .addOnFailureListener {
                    println("Error al subir la imagen: ${it.message}")
                }
        }
    }
}