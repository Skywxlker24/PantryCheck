package com.example.pantrycheck

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_compras")
data class ItemCompra(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val cantidad: Int,
    val precioEstimado: Double,
    var comprado: Boolean = false // Esto es para el Checkbox (marcar lo que ya echaste al carrito)
) {
    val subtotal: Double
        get() = cantidad * precioEstimado
}