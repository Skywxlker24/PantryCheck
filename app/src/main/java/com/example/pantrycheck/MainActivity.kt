package com.example.pantrycheck

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // Agregar productos
    private val listaPrueba = mutableListOf(
        Producto(Nombre = "Leche Entera", Cantidad = 2, PrecioEstimado = 28.50, FechaCaducidad = "16/02/2026"),
        Producto(Nombre = "Huevo (12 piezas)", Cantidad = 1, PrecioEstimado = 45.00, FechaCaducidad = "01/03/2026"),
        Producto(Nombre = "Pan de Caja", Cantidad = 1, PrecioEstimado = 52.00, FechaCaducidad = "20/02/2026")
    )

    // Declaracion del adaptador a nivel de clase para actualizarlo después
    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Configurar la lista
        val rvInventario: RecyclerView = findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(this)

        adapter = ProductoAdapter(listaPrueba)
        rvInventario.adapter = adapter

        // 3. Configurar boton de agregar
        val fabAgregar: android.view.View = findViewById(R.id.fabAgregar)
        fabAgregar.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    // 4. Ventana para agregar producto
    private fun mostrarDialogoAgregar() {
        val input = EditText(this)
        input.hint = "Ej. Galletas"
        input.setPadding(50, 40, 50, 40) // Espaciado para que se vea bien

        AlertDialog.Builder(this)
            .setTitle("Nuevo Producto")
            .setMessage("Escribe el nombre del producto:")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val nombreNuevo = input.text.toString()

                if (nombreNuevo.isNotBlank()) {
                    // Crea el producto con datos base y lo mete a la lista
                    val nuevoProducto = Producto(
                        Nombre = nombreNuevo,
                        Cantidad = 1,
                        PrecioEstimado = 15.00,
                        FechaCaducidad = "Próximamente"
                    )
                    listaPrueba.add(nuevoProducto)

                    // Avisa a la pantalla que hay un nuevo elemento al final
                    adapter.notifyItemInserted(listaPrueba.size - 1)
                    Toast.makeText(this, "Producto agregado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}