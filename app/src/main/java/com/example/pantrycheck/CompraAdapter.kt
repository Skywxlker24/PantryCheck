package com.example.pantrycheck

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompraAdapter(
    private val listaCompras: List<ItemCompra>,
    private val alCambiarEstado: (ItemCompra) -> Unit,
    private val alTocar: (ItemCompra) -> Unit // Funcion editar
) : RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    class CompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs tienen que coincidir con los de tu item_compra.xml
        val cbComprado: CheckBox = itemView.findViewById(R.id.cbComprado)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreCompra)
        val tvDetalles: TextView = itemView.findViewById(R.id.tvDetallesCompra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
        return CompraViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val item = listaCompras[position]

        // Evitar el listener para evitar bucles
        holder.cbComprado.setOnCheckedChangeListener(null)
        holder.cbComprado.isChecked = item.comprado

        holder.tvNombre.text = item.nombre
        val subtotal = item.cantidad * item.precioEstimado
        holder.tvDetalles.text = String.format("Cant: %d  |  Estimado: $%.2f", item.cantidad, subtotal)

        // Efecto de tachado si ya lo compraste
        if (item.comprado) {
            holder.tvNombre.paintFlags = holder.tvNombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvNombre.paintFlags = holder.tvNombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.cbComprado.setOnCheckedChangeListener { _, isChecked ->
            val itemModificado = item.copy(comprado = isChecked)
            alCambiarEstado(itemModificado)
        }

        // Logica editar al tocar
        holder.itemView.setOnClickListener {
            alTocar(item)
        }
    }

    override fun getItemCount(): Int = listaCompras.size
}