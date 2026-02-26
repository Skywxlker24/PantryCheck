package com.example.pantrycheck

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val especificaciones: String, // Especificaciones del producto
    val categoria: String, // Categoría del producto
    val cantidad: Int,
    val precioPorUnidad: Double, // Precio por unidad del producto
    val fechaCaducidad: String
) {
    // Suma del precio total dependiendo la cantidad de productos
    val precioTotal: Double
        get() = cantidad * precioPorUnidad
}
