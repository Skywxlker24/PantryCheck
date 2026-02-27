package com.example.pantrycheck

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ComprasFragment : Fragment() {

    private lateinit var adapter: CompraAdapter
    private var listaCompras = mutableListOf<ItemCompra>()
    private lateinit var database: AppDatabase
    private lateinit var tvTotalCompra: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_compras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        tvTotalCompra = view.findViewById(R.id.tvTotalCompra)

        val rvCompras: RecyclerView = view.findViewById(R.id.rvCompras)
        rvCompras.layoutManager = LinearLayoutManager(requireContext())

        adapter = CompraAdapter(listaCompras,
            alCambiarEstado = { itemModificado ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    database.compraDao().actualizar(itemModificado)
                    withContext(Dispatchers.Main) { adapter.notifyDataSetChanged() }
                }
            },
            alEliminar = { itemBorrar ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    database.compraDao().eliminar(itemBorrar)
                    cargarDatos()
                }
            }
        )
        rvCompras.adapter = adapter

        cargarDatos()

        val fabAgregar: FloatingActionButton = view.findViewById(R.id.fabAgregarCompra)
        fabAgregar.setOnClickListener {
            mostrarDialogoQueComprar()
        }
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val datos = database.compraDao().obtenerTodas()
            withContext(Dispatchers.Main) {
                listaCompras.clear()
                listaCompras.addAll(datos)
                adapter.notifyDataSetChanged()
                calcularTotalFijo()
            }
        }
    }

    private fun calcularTotalFijo() {
        var total = 0.0
        for (item in listaCompras) {
            total += item.subtotal
        }
        tvTotalCompra.text = String.format("$%.2f", total)
    }

    private fun mostrarDialogoQueComprar() {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        val inputNombre = EditText(requireContext()).apply { hint = "¿Qué vas a comprar? (Ej. Leche)" }
        val inputCantidad = EditText(requireContext()).apply {
            hint = "Cantidad"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(inputNombre)
        layout.addView(inputCantidad)

        AlertDialog.Builder(requireContext())
            .setTitle("Lista de Compras")
            .setView(layout)
            .setPositiveButton("Buscar Precios") { _, _ ->
                val nombre = inputNombre.text.toString()
                val cantStr = inputCantidad.text.toString()

                if (nombre.isNotBlank() && cantStr.isNotBlank()) {
                    mostrarSimuladorPrecios(nombre, cantStr.toInt())
                } else {
                    Toast.makeText(requireContext(), "Faltan datos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarSimuladorPrecios(nombre: String, cantidad: Int) {
        // Generamos precios falsos pero realistas
        val precioBase = 15.0 + (Math.random() * 50)

        val precioWalmart = String.format(Locale.US, "%.2f", precioBase).toDouble()
        val precioSoriana = String.format(Locale.US, "%.2f", precioBase + (Math.random() * 5)).toDouble()
        val precioHEB = String.format(Locale.US, "%.2f", precioBase + (Math.random() * 8)).toDouble()

        // ¡EL ERROR ESTABA AQUÍ! Usamos 'to' en lugar de 'a'
        val precios = mapOf("Walmart" to precioWalmart, "Soriana" to precioSoriana, "HEB" to precioHEB)
        val mejorTienda = precios.minByOrNull { it.value }

        val mensaje = """
            Buscando precios en la red...
            
            🏪 Soriana: $${precioSoriana}
            🏪 HEB: $${precioHEB}
            🏪 Walmart: $${precioWalmart}
            
            ✅ ¡La mejor opción es ${mejorTienda?.key}!
            ¿Deseas agregarlo con el precio estimado de $${mejorTienda?.value}?
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Simulador de Ahorro")
            .setMessage(mensaje)
            .setPositiveButton("Agregar a la lista") { _, _ ->
                val nuevoItem = ItemCompra(
                    nombre = nombre,
                    cantidad = cantidad,
                    precioEstimado = mejorTienda?.value ?: 0.0
                )

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    database.compraDao().insertar(nuevoItem)
                    cargarDatos()
                }
                Toast.makeText(requireContext(), "Agregado al carrito", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}