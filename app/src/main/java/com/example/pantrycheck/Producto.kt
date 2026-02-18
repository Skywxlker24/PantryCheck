package com.example.pantrycheck

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val Nombre: String,
    val Cantidad: Int,
    val PrecioEstimado: Double,
    val FechaCaducidad: String
)

