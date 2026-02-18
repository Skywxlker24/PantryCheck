package com.example.pantrycheck

import androidx.room.*

@Dao
interface ProductoDao {
    @Insert
    suspend fun insertar(producto: Producto) //

    @Update
    suspend fun actualizar(producto: Producto) //

    @Delete
    suspend fun eliminar(producto: Producto) //

    @Query("SELECT * FROM tabla_productos")
    suspend fun obtenerTodos(): List<Producto>
}