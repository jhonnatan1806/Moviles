package com.example.pc3moviles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class FotoAdapter(private val context: Context, private val fotos: List<ClaseFoto>) : BaseAdapter() {

    override fun getCount(): Int {
        return fotos.size
    }

    override fun getItem(position: Int): Any {
        return fotos[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_view, parent, false)

        // Obtener elementos del diseño
        val ivFoto = view.findViewById<ImageView>(R.id.ivFoto)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombre)
        val tvHayRostro = view.findViewById<TextView>(R.id.tvHayRostro)

        // Obtener la foto actual
        val foto = fotos[position]

        // Configurar los datos
        tvNombre.text = foto.nombre
        tvHayRostro.text = foto.hayrostro

        // Agregar un parámetro único para evitar la caché
        val urlConCacheBusting = "${foto.miUrl}?timestamp=${System.currentTimeMillis()}"
        Glide.with(context).load(urlConCacheBusting).into(ivFoto)

        return view
    }
}