package com.example.pantrycheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(
    private val listaProductos: List<Producto>,
    private val alTocarProducto: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProducto)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaProducto)
        val tvCaducidad: TextView = itemView.findViewById(R.id.tvCaducidadProducto)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidadProducto)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = "${producto.categoria} | ${producto.especificaciones}"
        holder.tvCaducidad.text = producto.fechaCaducidad

        // ¡AQUÍ ESTÁ EL CAMBIO! Regresamos a tu formato original
        holder.tvCantidad.text = "Cantidad: ${producto.cantidad}"

        holder.tvPrecio.text = String.format("$%.2f", producto.precioPorUnidad)

        holder.itemView.setOnClickListener {
            alTocarProducto(producto)
        }
    }

    override fun getItemCount(): Int = listaProductos.size
}