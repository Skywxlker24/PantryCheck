package com.example.pantrycheck

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProductoAdapter(
    private val listaProductos: List<Producto>,
    private val alTocarProducto: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProducto)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaProducto)
        val tvCaducidad: TextView = itemView.findViewById(R.id.tvCaducidadProducto)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidadProducto)

        val tvPrecioUnidad: TextView = itemView.findViewById(R.id.tvPrecioUnidad)
        val tvPrecioTotal: TextView = itemView.findViewById(R.id.tvPrecioTotal)

        val vSemaforo: View = itemView.findViewById(R.id.vSemaforo)
        // Conectamos el nuevo renglón de alertas
        val tvEstadoCaducidad: TextView = itemView.findViewById(R.id.tvEstadoCaducidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = "${producto.categoria} | ${producto.especificaciones}"
        holder.tvCantidad.text = "Cantidad: ${producto.cantidad}"

        // Inyectamos los precios
        val precioUnitario = producto.precioPorUnidad
        val precioTotal = precioUnitario * producto.cantidad
        holder.tvPrecioUnidad.text = String.format("Precio ud: $%.2f", precioUnitario)
        holder.tvPrecioTotal.text = String.format("Total: $%.2f", precioTotal)

        // LÓGICA DE SEMÁFORO LATERAL Y ESTADOS
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hoyStr = sdf.format(Date())
            val hoy = sdf.parse(hoyStr) ?: Date()
            val fechaCad = sdf.parse(producto.fechaCaducidad) ?: Date()

            val diffMilisegundos = fechaCad.time - hoy.time
            val diasRestantes = TimeUnit.MILLISECONDS.toDays(diffMilisegundos).toInt()

            // 1. Siempre mostramos la fecha original limpia en su lugar
            holder.tvCaducidad.text = producto.fechaCaducidad

            // 2. Controlamos el color de la barra lateral y el mensaje del nuevo renglón
            when {
                diasRestantes < 0 -> {
                    // YA CADUCÓ (Gris Oscuro)
                    holder.vSemaforo.setBackgroundColor(Color.parseColor("#757575"))
                    holder.tvEstadoCaducidad.text = "¡Producto Caducado!"
                    holder.tvEstadoCaducidad.setTextColor(Color.parseColor("#757575"))
                    holder.tvEstadoCaducidad.visibility = View.VISIBLE
                }
                diasRestantes in 0..3 -> {
                    // ROJO: Urgente (0 a 3 días)
                    holder.vSemaforo.setBackgroundColor(Color.parseColor("#E53935"))
                    holder.tvEstadoCaducidad.text = "¡Alerta: Próximo a vencer!"
                    holder.tvEstadoCaducidad.setTextColor(Color.parseColor("#E53935"))
                    holder.tvEstadoCaducidad.visibility = View.VISIBLE
                }
                diasRestantes in 4..7 -> {
                    // NARANJA: Precaución (4 a 7 días)
                    holder.vSemaforo.setBackgroundColor(Color.parseColor("#F57C00"))
                    holder.tvEstadoCaducidad.text = "Precaución: Se acerca la fecha"
                    holder.tvEstadoCaducidad.setTextColor(Color.parseColor("#F57C00"))
                    holder.tvEstadoCaducidad.visibility = View.VISIBLE
                }
                else -> {
                    // VERDE LIMA: Buen estado (Más de 7 días)
                    holder.vSemaforo.setBackgroundColor(Color.parseColor("#8CC63F"))
                    // Como el producto está bien, ocultamos el mensaje para que la tarjeta se vea más limpia
                    holder.tvEstadoCaducidad.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // En caso de error de fecha
            holder.vSemaforo.setBackgroundColor(Color.parseColor("#8CC63F"))
            holder.tvCaducidad.text = producto.fechaCaducidad
            holder.tvEstadoCaducidad.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { alTocarProducto(producto) }
    }

    override fun getItemCount(): Int = listaProductos.size
}