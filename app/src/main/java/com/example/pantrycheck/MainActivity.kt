package com.example.pantrycheck

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ProductoAdapter
    private var listaProductos = mutableListOf<Producto>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa la Base de Datos
        database = AppDatabase.getDatabase(this)

        // Configuracion de la lista UI
        val rvInventario: RecyclerView = findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapter(listaProductos)
        rvInventario.adapter = adapter

        // Activar función de deslizar para borrar
        configurarDeslizarParaBorrar(rvInventario)

        // Cargar los datos desde SQLite al abrir la app
        cargarDatosDesdeSQLite()

        // Botón flotante para agregar
        val fabAgregar: View = findViewById(R.id.fabAgregar)
        fabAgregar.setOnClickListener {
            mostrarDialogoPaso1()
        }
    }

    private fun cargarDatosDesdeSQLite() {
        lifecycleScope.launch(Dispatchers.IO) {
            val datosGuardados = database.productoDao().obtenerTodos()
            withContext(Dispatchers.Main) {
                listaProductos.clear()
                listaProductos.addAll(datosGuardados)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun configurarDeslizarParaBorrar(recyclerView: RecyclerView) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicion = viewHolder.adapterPosition
                val productoSeleccionado = listaProductos[posicion]

                lifecycleScope.launch(Dispatchers.IO) {
                    database.productoDao().eliminar(productoSeleccionado)
                    withContext(Dispatchers.Main) {
                        listaProductos.removeAt(posicion)
                        adapter.notifyItemRemoved(posicion)
                        Toast.makeText(this@MainActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun mostrarDialogoPaso1() {
        // nuevo layout para agrupar los dos campos
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre (Ej. Leche)"

        val inputCantidad = EditText(this)
        inputCantidad.hint = "Cantidad (Ej. 2)"
        // Teclado numerico para cantidad
        inputCantidad.inputType = InputType.TYPE_CLASS_NUMBER

        layout.addView(inputNombre)
        layout.addView(inputCantidad)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Producto")
            .setMessage("Ingresa los datos del producto:")
            .setView(layout)
            .setPositiveButton("Siguiente") { _, _ ->
                val nombre = inputNombre.text.toString()
                val cantidadStr = inputCantidad.text.toString()

                if (nombre.isNotBlank() && cantidadStr.isNotBlank()) {
                    val cantidadFina = cantidadStr.toInt()
                    mostrarCalendarioPaso2(nombre, cantidadFina)
                } else {
                    Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarCalendarioPaso2(nombreProducto: String, cantidadProducto: Int) {
        val calendario = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val fechaFormateada = "$dayOfMonth/${month + 1}/$year"

            val nuevoProducto = Producto(
                nombre = nombreProducto,
                cantidad = cantidadProducto, // Ya guarda la cantidad real
                precioEstimado = 25.50, // Precio fijo por ahora
                fechaCaducidad = fechaFormateada
            )

            // Guardar en la base de datos
            lifecycleScope.launch(Dispatchers.IO) {
                database.productoDao().insertar(nuevoProducto)
                cargarDatosDesdeSQLite()
            }
            Toast.makeText(this, "Guardado en la despensa", Toast.LENGTH_SHORT).show()

        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))

        // Titulo de calendario
        datePickerDialog.setTitle("Inserte fecha de caducidad")
        datePickerDialog.show()
    }
}