package com.example.pc3moviles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.example.pc3moviles.databinding.ListViewBinding

class PhotoListAdapter(private val context: Context, private val fotos: List<PhotoModel>) : BaseAdapter() {

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
        val binding: ListViewBinding = if (convertView == null) {
            ListViewBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            convertView.tag as ListViewBinding
        }

        // Obtener la foto actual
        val foto = fotos[position]


        binding.tvNombre.text = foto.nombre
        binding.tvHayRostro.text = foto.hayrostro

        // Agregar un parámetro único para evitar la caché
        val urlConCacheBusting = "${foto.miUrl}?timestamp=${System.currentTimeMillis()}"

        // Cargar la imagen con Glide
        Glide.with(context).load(urlConCacheBusting).into(binding.ivPhoto)

        // Asociar el binding al convertView para reutilización
        if (convertView == null) {
            binding.root.tag = binding
        }

        return binding.root
    }
}