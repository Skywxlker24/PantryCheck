package com.example.pantrycheck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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

        adapter = CompraAdapter(listaCompras,
            alCambiarEstado = { itemModificado ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    database.compraDao().actualizar(itemModificado)
                    withContext(Dispatchers.Main) { adapter.notifyDataSetChanged() }
                }
            },
            alEliminar = { itemBorrar ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar de la lista")
                    .setMessage("¿Estás seguro de que deseas quitar '${itemBorrar.nombre}'?")
                    .setCancelable(false)
                    .setPositiveButton("Sí, eliminar") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            database.compraDao().eliminar(itemBorrar)
                            cargarDatos()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        rvCompras.adapter = adapter
    }

    // ¡CLAVE! Actualiza la lista cada vez que regresas a esta pestaña
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
}