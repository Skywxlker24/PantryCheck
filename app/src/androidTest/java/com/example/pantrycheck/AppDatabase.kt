package com.example.pantrycheck

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Producto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
}