package com.example.pantrycheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Conexion lista de datos y el diseño visual (XML)
class ProductoAdapter(private val lista: List<Producto>) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    //IDs de las vistas en el XML
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val fecha: TextView = view.findViewById(R.id.tvFecha)
        val cantidad: TextView = view.findViewById(R.id.tvCantidad)
        val precio: TextView = view.findViewById(R.id.tvPrecio)
    }

    // Tarjeta del producto
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ViewHolder(view)
    }

    // Informacion para la tarjeta del producto
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]
        holder.nombre.text = producto.nombre
        holder.fecha.text = "Vence: ${producto.fechaCaducidad}"
        holder.cantidad.text = "Cant: ${producto.cantidad}"

        // Decimales
        holder.precio.text = String.format("$%.2f", producto.precioEstimado)
    }

    override fun getItemCount() = lista.size
}