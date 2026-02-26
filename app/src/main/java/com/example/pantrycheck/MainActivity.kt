package com.example.pantrycheck

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
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

        // 1. Conexion a base de datos
        database = AppDatabase.getDatabase(this)

        // 2. Crear lista de productos
        val rvInventario: RecyclerView = findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(this)

        // 3. Crear adaptador
        adapter = ProductoAdapter(listaProductos) { productoTocado ->
            mostrarDialogoEdicionPaso1(productoTocado)
        }
        rvInventario.adapter = adapter

        // 4. Cargar datos desde SQLite
        configurarDeslizarParaBorrar(rvInventario)
        cargarDatosDesdeSQLite()

        // 5. Boton agregar
        val fabAgregar: View = findViewById(R.id.fabAgregar)
        fabAgregar.setOnClickListener {
            mostrarDialogoAgregarPaso1()
        }
    }

    private fun cargarDatosDesdeSQLite() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val datosGuardados = database.productoDao().obtenerTodos()
                withContext(Dispatchers.Main) {
                    listaProductos.clear()
                    listaProductos.addAll(datosGuardados)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 6. Swipe to delete
    private fun configurarDeslizarParaBorrar(recyclerView: RecyclerView) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicion = viewHolder.adapterPosition
                val productoSeleccionado = listaProductos[posicion]

                // Mostrar diálogo de confirmación
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Eliminar Producto")
                    .setMessage("¿Estás seguro de que deseas tirar '${productoSeleccionado.nombre}' de tu despensa?")
                    .setCancelable(false)
                    .setPositiveButton("Confirmar") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            database.productoDao().eliminar(productoSeleccionado)
                            withContext(Dispatchers.Main) {
                                listaProductos.removeAt(posicion)
                                adapter.notifyItemRemoved(posicion)
                                Toast.makeText(this@MainActivity, "Eliminado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        adapter.notifyItemChanged(posicion)
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    // 7. Agregar
    private fun mostrarDialogoAgregarPaso1() {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        val inputNombre = EditText(this).apply { hint = "Nombre (Ej. Leche)" }
        val inputEsp = EditText(this).apply { hint = "Especificaciones (Ej. Deslactosada)" }
        val inputCat = EditText(this).apply { hint = "Categoría (Ej. Lácteos)" }
        val inputCantidad = EditText(this).apply {
            hint = "Cantidad (Ej. 2)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val inputPrecio = EditText(this).apply {
            hint = "Precio Unitario (Ej. 28.50)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(inputNombre)
        layout.addView(inputEsp)
        layout.addView(inputCat)
        layout.addView(inputCantidad)
        layout.addView(inputPrecio)
        scrollView.addView(layout)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Producto")
            .setView(scrollView)
            .setPositiveButton("Siguiente") { _, _ ->
                val nombre = inputNombre.text.toString()
                val esp = inputEsp.text.toString()
                val cat = inputCat.text.toString()
                val cantStr = inputCantidad.text.toString()
                val precioStr = inputPrecio.text.toString()

                if (nombre.isNotBlank() && cantStr.isNotBlank() && precioStr.isNotBlank()) {
                    val cantidadFina = cantStr.toIntOrNull() ?: 1
                    val precioFino = precioStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    mostrarCalendarioPaso2(null, nombre, esp, cat, cantidadFina, precioFino)
                } else {
                    Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 8. Editar
    private fun mostrarDialogoEdicionPaso1(producto: Producto) {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        val inputNombre = EditText(this).apply { setText(producto.nombre) }
        val inputEsp = EditText(this).apply { setText(if(producto.especificaciones=="N/A") "" else producto.especificaciones) }
        val inputCat = EditText(this).apply { setText(if(producto.categoria=="Sin Categoría") "" else producto.categoria) }
        val inputCantidad = EditText(this).apply {
            setText(producto.cantidad.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val inputPrecio = EditText(this).apply {
            setText(producto.precioPorUnidad.toString())
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(inputNombre)
        layout.addView(inputEsp)
        layout.addView(inputCat)
        layout.addView(inputCantidad)
        layout.addView(inputPrecio)
        scrollView.addView(layout)

        AlertDialog.Builder(this)
            .setTitle("Editar Producto")
            .setView(scrollView)
            .setPositiveButton("Siguiente") { _, _ ->
                val nombre = inputNombre.text.toString()
                val esp = inputEsp.text.toString()
                val cat = inputCat.text.toString()
                val cantStr = inputCantidad.text.toString()
                val precioStr = inputPrecio.text.toString()

                if (nombre.isNotBlank() && cantStr.isNotBlank() && precioStr.isNotBlank()) {
                    val cantidadFina = cantStr.toIntOrNull() ?: 1
                    val precioFino = precioStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    mostrarCalendarioPaso2(producto, nombre, esp, cat, cantidadFina, precioFino)
                } else {
                    Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 9. Calendario
    private fun mostrarCalendarioPaso2(productoViejo: Producto?, nombre: String, esp: String, cat: String, cantidad: Int, precio: Double) {
        val calendario = Calendar.getInstance()

        var anio = calendario.get(Calendar.YEAR)
        var mes = calendario.get(Calendar.MONTH)
        var dia = calendario.get(Calendar.DAY_OF_MONTH)

        if (productoViejo != null) {
            val partes = productoViejo.fechaCaducidad.split("/")
            if (partes.size == 3) {
                dia = partes[0].toIntOrNull() ?: dia
                mes = (partes[1].toIntOrNull() ?: (mes + 1)) - 1
                anio = partes[2].toIntOrNull() ?: anio
            }
        }

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            try {
                val fechaFormateada = "$dayOfMonth/${month + 1}/$year"
                val idAsignar = productoViejo?.id ?: 0

                val productoFinal = Producto(
                    id = idAsignar,
                    nombre = nombre,
                    especificaciones = esp.ifBlank { "N/A" },
                    categoria = cat.ifBlank { "Sin Categoría" },
                    cantidad = cantidad,
                    precioPorUnidad = precio,
                    fechaCaducidad = fechaFormateada
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    if (productoViejo == null) {
                        database.productoDao().insertar(productoFinal)
                    } else {
                        database.productoDao().actualizar(productoFinal)
                    }
                    cargarDatosDesdeSQLite()
                }
                Toast.makeText(this, if (productoViejo == null) "Guardado" else "Actualizado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }, anio, mes, dia)

        datePickerDialog.setTitle(if (productoViejo == null) "Inserte fecha de caducidad" else "Modificar fecha")
        datePickerDialog.show()
    }
}