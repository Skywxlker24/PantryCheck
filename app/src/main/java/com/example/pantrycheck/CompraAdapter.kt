package com.example.pantrycheck

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompraAdapter(
    private val lista: List<ItemCompra>,
    private val alCambiarEstado: (ItemCompra) -> Unit,
    private val alEliminar: (ItemCompra) -> Unit
) : RecyclerView.Adapter<CompraAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.cbComprado)
        val nombre: TextView = view.findViewById(R.id.tvNombreCompra)
        val detalle: TextView = view.findViewById(R.id.tvDetalleCompra)
        val subtotal: TextView = view.findViewById(R.id.tvSubtotalCompra)
        val btnEliminar: ImageView = view.findViewById(R.id.btnEliminarCompra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.nombre.text = item.nombre
        holder.detalle.text = String.format("Cant: %d | Estimado: $%.2f", item.cantidad, item.precioEstimado)
        holder.subtotal.text = String.format("$%.2f", item.subtotal)

        // Desactiva el listener para evitar bucles
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.comprado

        // Si se marca como comprado, tachar el nombre
        if (item.comprado) {
            holder.nombre.paintFlags = holder.nombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.nombre.setTextColor(android.graphics.Color.GRAY)
        } else {
            holder.nombre.paintFlags = holder.nombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.nombre.setTextColor(android.graphics.Color.BLACK)
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            item.comprado = isChecked
            alCambiarEstado(item)
        }

        holder.btnEliminar.setOnClickListener {
            alEliminar(item)
        }
    }

    override fun getItemCount() = lista.size
}