package com.example.pantrycheck

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class InventarioFragment : Fragment() {

    private lateinit var adapter: ProductoAdapter
    private var listaProductos = mutableListOf<Producto>()
    private lateinit var database: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Usamos requireContext() porque estamos dentro de un Fragmento
        database = AppDatabase.getDatabase(requireContext())

        val rvInventario: RecyclerView = view.findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProductoAdapter(listaProductos) { productoTocado ->
            mostrarDialogoEdicionPaso1(productoTocado)
        }
        rvInventario.adapter = adapter

        configurarDeslizarParaBorrar(rvInventario)
        cargarDatosDesdeSQLite()

        val fabAgregar: FloatingActionButton = view.findViewById(R.id.fabAgregar)
        fabAgregar.setOnClickListener {
            mostrarDialogoAgregarPaso1()
        }
    }

    // Cargamos los datos desde la base de datos SQLite
    private fun cargarDatosDesdeSQLite() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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

    // Swipe to delete
    private fun configurarDeslizarParaBorrar(recyclerView: RecyclerView) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicion = viewHolder.adapterPosition
                val productoSeleccionado = listaProductos[posicion]

                // Confirmacion swipe to delete
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Producto")
                    .setMessage("¿Estás seguro de que deseas tirar '${productoSeleccionado.nombre}' de tu despensa?")
                    .setCancelable(false)
                    .setPositiveButton("Sí, eliminar") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            database.productoDao().eliminar(productoSeleccionado)
                            withContext(Dispatchers.Main) {
                                listaProductos.removeAt(posicion)
                                adapter.notifyItemRemoved(posicion)
                                Toast.makeText(requireContext(), "Eliminado", Toast.LENGTH_SHORT).show()
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

    // Agregar producto
    private fun mostrarDialogoAgregarPaso1() {
        val scrollView = ScrollView(requireContext())
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        val inputNombre = EditText(requireContext()).apply { hint = "Nombre (Ej. Leche)" }
        val inputEsp = EditText(requireContext()).apply { hint = "Especificaciones (Ej. Deslactosada)" }
        val inputCat = EditText(requireContext()).apply { hint = "Categoría (Ej. Lácteos)" }
        val inputCantidad = EditText(requireContext()).apply {
            hint = "Cantidad (Ej. 2)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val inputPrecio = EditText(requireContext()).apply {
            hint = "Precio Unitario (Ej. 28.50)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(inputNombre)
        layout.addView(inputEsp)
        layout.addView(inputCat)
        layout.addView(inputCantidad)
        layout.addView(inputPrecio)
        scrollView.addView(layout)

        AlertDialog.Builder(requireContext())
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
                    Toast.makeText(requireContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Editar producto
    private fun mostrarDialogoEdicionPaso1(producto: Producto) {
        val scrollView = ScrollView(requireContext())
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        val inputNombre = EditText(requireContext()).apply { setText(producto.nombre) }
        val inputEsp = EditText(requireContext()).apply { setText(if(producto.especificaciones=="N/A") "" else producto.especificaciones) }
        val inputCat = EditText(requireContext()).apply { setText(if(producto.categoria=="Sin Categoría") "" else producto.categoria) }
        val inputCantidad = EditText(requireContext()).apply {
            setText(producto.cantidad.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val inputPrecio = EditText(requireContext()).apply {
            setText(producto.precioPorUnidad.toString())
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(inputNombre)
        layout.addView(inputEsp)
        layout.addView(inputCat)
        layout.addView(inputCantidad)
        layout.addView(inputPrecio)
        scrollView.addView(layout)

        AlertDialog.Builder(requireContext())
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
                    Toast.makeText(requireContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Mostrar calendario
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

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
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

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    if (productoViejo == null) {
                        database.productoDao().insertar(productoFinal)
                    } else {
                        database.productoDao().actualizar(productoFinal)
                    }
                    cargarDatosDesdeSQLite()
                }
                Toast.makeText(requireContext(), if (productoViejo == null) "Guardado" else "Actualizado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }, anio, mes, dia)

        datePickerDialog.setTitle(if (productoViejo == null) "Inserte fecha de caducidad" else "Modificar fecha")
        datePickerDialog.show()
    }
}