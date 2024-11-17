package com.example.pc3moviles
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseHelper {

    companion object {
        // Guardar la clase en Realtime Database
        fun guardarClaseEnDatabase(claseFoto: ClaseFoto, callback: (Boolean) -> Unit) {
            // Referencia a la base de datos
            val databaseRef = FirebaseDatabase.getInstance().reference

            // Generar un nodo único para cada entrada
            val nuevaFotoRef = databaseRef.child("fotos").push()

            // Guardar los datos
            nuevaFotoRef.setValue(claseFoto)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("ClaseFoto guardada exitosamente.")
                        callback(true)
                    } else {
                        println("Error al guardar ClaseFoto: ${task.exception?.message}")
                        callback(false)
                    }
                }
        }
    }
}