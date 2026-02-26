package com.example.pantrycheck

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDao {
    @Query("SELECT * FROM tabla_productos")
    fun obtenerTodos(): List<Producto>

    @Insert
    fun insertar(producto: Producto)

    @Delete
    fun eliminar(producto: Producto)

    @Update
    fun actualizar(producto: Producto)
}