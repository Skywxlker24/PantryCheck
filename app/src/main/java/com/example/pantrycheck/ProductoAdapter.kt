package com.example.pantrycheck

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProductoAdapter(
    private val lista: List<Producto>,
    private val alHacerClic: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val fecha: TextView = view.findViewById(R.id.tvFecha)
        val cantidad: TextView = view.findViewById(R.id.tvCantidad)
        val precio: TextView = view.findViewById(R.id.tvPrecio)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditar)

        // Conectamos la barra lateral de color (El semáforo)
        val indicadorEstado: View = view.findViewById(R.id.indicadorEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]

        // Textos basicos
        holder.nombre.text = "${producto.nombre} (${producto.categoria})"
        holder.cantidad.text = "Cant: ${producto.cantidad} | ${producto.especificaciones}"
        holder.precio.text = String.format("$%.2f c/u\nTotal: $%.2f", producto.precioPorUnidad, producto.precioTotal)

        // Logica para el color del semáforo y el texto de la fecha
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        try {
            val fechaCaducidad = sdf.parse(producto.fechaCaducidad)

            // Se calcula la fecha actual
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (fechaCaducidad != null) {
                // Calculamos la diferencia en días
                val diffEnMilisegundos = fechaCaducidad.time - hoy.time
                val diasRestantes = TimeUnit.DAYS.convert(diffEnMilisegundos, TimeUnit.MILLISECONDS)

                when {
                    diasRestantes < 0 -> {
                        // 1. Ya caducó (Gris Claro)
                        holder.indicadorEstado.setBackgroundColor(Color.parseColor("#424242"))
                        holder.fecha.setTextColor(Color.parseColor("#424242"))
                        holder.fecha.text = "¡CADUCADO! (${producto.fechaCaducidad})"
                    }
                    diasRestantes in 0..3 -> {
                        // 2. Por caducar (Rojo)
                        holder.indicadorEstado.setBackgroundColor(Color.parseColor("#F44336"))
                        holder.fecha.setTextColor(Color.parseColor("#F44336"))
                        holder.fecha.text = "Vence pronto: ${producto.fechaCaducidad}"
                    }
                    diasRestantes in 4..7 -> {
                        // 3. Advertencia (Naranja)
                        holder.indicadorEstado.setBackgroundColor(Color.parseColor("#FF9800"))
                        holder.fecha.setTextColor(Color.parseColor("#757575")) // Texto gris normal
                        holder.fecha.text = "Vence: ${producto.fechaCaducidad}"
                    }
                    else -> {
                        // 4. Seguro (Verde)
                        holder.indicadorEstado.setBackgroundColor(Color.parseColor("#4CAF50"))
                        holder.fecha.setTextColor(Color.parseColor("#757575")) // Texto gris normal
                        holder.fecha.text = "Vence: ${producto.fechaCaducidad}"
                    }
                }
            }
        } catch (e: Exception) {
            // Si por alguna razón la fecha viene mal escrita, no crashea, solo se pinta gris claro
            holder.indicadorEstado.setBackgroundColor(Color.parseColor("#9E9E9E"))
            holder.fecha.text = "Vence: ${producto.fechaCaducidad}"
            e.printStackTrace()
        }

        // Boton de editar
        holder.btnEditar.setOnClickListener {
            alHacerClic(producto)
        }
    }

    override fun getItemCount() = lista.size
}