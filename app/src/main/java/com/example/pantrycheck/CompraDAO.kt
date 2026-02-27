package com.example.pantrycheck

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CompraDao {
    @Query("SELECT * FROM tabla_compras")
    fun obtenerTodas(): List<ItemCompra>

    @Insert
    fun insertar(item: ItemCompra)

    @Delete
    fun eliminar(item: ItemCompra)

    @Update
    fun actualizar(item: ItemCompra)
}