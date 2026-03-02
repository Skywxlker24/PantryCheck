package com.example.pantrycheck

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // 1. Configuracion editar
        adapter = CompraAdapter(listaCompras,
            alCambiarEstado = { itemModificado ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    database.compraDao().actualizar(itemModificado)
                    withContext(Dispatchers.Main) { cargarDatos() } // Recargamos para actualizar total
                }
            },
            alTocar = { itemTocado ->
                // Al tocar, mandamos los datos al formulario para editar
                val intent = Intent(requireContext(), FormularioCompraActivity::class.java).apply {
                    putExtra("ID", itemTocado.id)
                    putExtra("NOMBRE", itemTocado.nombre)
                    putExtra("CANTIDAD", itemTocado.cantidad)
                }
                startActivity(intent)
            }
        )
        rvCompras.adapter = adapter

        // 2. Swipe to delete
        configurarDeslizarParaBorrar(rvCompras)
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
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

    private fun configurarDeslizarParaBorrar(recyclerView: RecyclerView) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicion = viewHolder.adapterPosition
                val itemSeleccionado = listaCompras[posicion]

                AlertDialog.Builder(requireContext())
                    .setTitle("Quitar de la lista")
                    .setMessage("¿Estás seguro de que deseas eliminar '${itemSeleccionado.nombre}'?")
                    .setCancelable(false)
                    .setPositiveButton("Confirmar") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            database.compraDao().eliminar(itemSeleccionado)
                            withContext(Dispatchers.Main) {
                                listaCompras.removeAt(posicion)
                                adapter.notifyItemRemoved(posicion)
                                calcularTotalFijo() // Actualiza el total flotante
                                Toast.makeText(requireContext(), "Eliminado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        adapter.notifyItemChanged(posicion) // Devuelve el elemento a su lugar si cancelas
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }
}