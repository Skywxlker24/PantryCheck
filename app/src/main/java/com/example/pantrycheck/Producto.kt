package com.example.pantrycheck

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Room llenará este ID automáticamente
    val nombre: String,
    val cantidad: Int,
    val precioEstimado: Double,
    val fechaCaducidad: String
)

