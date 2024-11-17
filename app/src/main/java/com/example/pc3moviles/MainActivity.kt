package com.example.pc3moviles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FotoAdapter
    private val fotos = mutableListOf<ClaseFoto>()
    private var uriImagenTemporal: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var nombreArchivo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = FotoAdapter(this, fotos)
        val listView = findViewById<ListView>(R.id.lvFotos)
        listView.adapter = adapter

        cargarDatosDesdeFirebase()

        val buttonCamara = findViewById<Button>(R.id.btnCamara)
        buttonCamara.setOnClickListener {
            abrirCamara()
        }

        val buttonEnviar = findViewById<Button>(R.id.btnEnviar)
        buttonEnviar.setOnClickListener {
            if (uriImagenTemporal != null) {
                val nombre = this.nombreArchivo
                val hayrostro = "SI"

                FirebaseStorageHelper.subirImagenAlStorage(uriImagenTemporal!!) { urlImagen ->
                    val nuevaFoto = ClaseFoto(nombre.toString(), urlImagen, hayrostro)
                    FirebaseDatabaseHelper.guardarClaseEnDatabase(nuevaFoto) { exito ->
                        if (exito) {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } else {
                println("No se ha capturado una imagen.")
            }
        }
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val archivoImagen = crearArchivoTemporal(this)
            uriImagenTemporal = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                archivoImagen
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagenTemporal)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            println("No se encontró ninguna aplicación de cámara.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageView = findViewById<ImageView>(R.id.imageView)
            imageView.setImageURI(uriImagenTemporal)

            // Actualizar el TextView con el nombre del archivo
            val tvNombre = findViewById<TextView>(R.id.tvNombre)
            tvNombre.text = nombreArchivo
        }
    }

    private fun crearArchivoTemporal(context: Context): File {
        // obtenemos el nombre que enviaremos a la base de datos
        val formatoFecha = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
        val fechaActual = Date()
        nombreArchivo = "app_${formatoFecha.format(fechaActual)}"

        val nombreArchivoTemporal = "temp_image"
        val directorio = context.getExternalFilesDir(null) ?: context.cacheDir
        return File.createTempFile(nombreArchivoTemporal, ".jpg", directorio)
    }

    private fun cargarDatosDesdeFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("fotos")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fotos.clear()
                for (fotoSnapshot in snapshot.children) {
                    val foto = fotoSnapshot.getValue(ClaseFoto::class.java)
                    if (foto != null) {
                        fotos.add(foto)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al cargar datos: ${error.message}")
            }
        })
    }
}