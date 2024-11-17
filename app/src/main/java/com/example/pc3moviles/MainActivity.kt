package com.example.pc3moviles

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pc3moviles.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var mBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    private val photoList = mutableListOf<PhotoModel>()
    private lateinit var photoListAdapter: PhotoListAdapter
    private var nombreArchivo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el ListView con el adaptador
        photoListAdapter = PhotoListAdapter(this, photoList)
        binding.lvPhotos.adapter = photoListAdapter

        // Cargar datos desde Firebase
        loadPhotosFromFirebase()

        // Llamada para tomar una foto
        val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                mBitmap = it
                binding.imageView.setImageBitmap(it)
                nombreArchivo = generateFileName() // Generar nombre único para la foto
                binding.tvNombre.text = nombreArchivo // Mostrar el nombre en el TextView
                Toast.makeText(this, "¡Foto capturada!", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }

        // Botón para capturar la foto con la cámara
        binding.btnCapturar.setOnClickListener {
            takePictureLauncher.launch(null) // Lanza la cámara y captura la foto
        }

        // Botón para enviar la imagen y realizar predicción
        binding.btnEnviar.setOnClickListener {
            if (mBitmap != null) {
                Toast.makeText(applicationContext, "Procesando...", Toast.LENGTH_SHORT).show()
                uploadImageAndPredict(mBitmap, nombreArchivo)
            } else {
                Toast.makeText(applicationContext, "No se ha capturado una foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "IMG_${dateFormat.format(Date())}.jpg"
    }

    private fun uploadImageAndPredict(bitmap: Bitmap, fileName: String?) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
        val body = MultipartBody.Part.createFormData("file", fileName ?: "image.jpeg", requestBody)

        val call = RetrofitPhoto.instance.predict(body)
        call.enqueue(object : Callback<ResponseData> {
            override fun onResponse(
                call: Call<ResponseData>,
                response: Response<ResponseData>
            ) {
                if (response.isSuccessful) {
                    val prediction = response.body()?.prediction ?: "NO"
                    saveImageToFirebase(bitmap, prediction, fileName)
                } else {
                    Toast.makeText(this@MainActivity, "Error en la predicción, intenta nuevamente.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveImageToFirebase(bitmap: Bitmap, prediction: String, fileName: String?) {
        // Generar el nombre único basado en la fecha y hora
        val nombreImagen = fileName ?: "sin_nombre.jpg"

        // Ruta en la carpeta "imagenes/"
        val storageRef = FirebaseStorage.getInstance().reference.child("images/$nombreImagen")

        // Convertir el Bitmap a un array de bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Subir la imagen a Firebase Storage
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            // Obtener la URL de descarga
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Guardar los datos en la base de datos
                savePhotoToDatabase(nombreImagen, uri.toString(), prediction)
            }
        }.addOnFailureListener { e ->
            // Manejar errores en la subida
            Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoToDatabase(nombre: String, url: String, prediction: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("photos")
        val photo = PhotoModel(
            nombre = nombre,
            miUrl = url,
            hayrostro = prediction
        )
        databaseRef.push().setValue(photo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Foto guardada correctamente.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al guardar la foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPhotosFromFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("photos")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                photoList.clear()
                for (photoSnapshot in snapshot.children) {
                    val photo = photoSnapshot.getValue(PhotoModel::class.java)
                    if (photo != null) {
                        photoList.add(photo)
                    }
                }
                photoListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}