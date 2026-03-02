package com.example.pantrycheck

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventarioFragment : Fragment() {

    private lateinit var adapter: ProductoAdapter
    private var listaProductos = mutableListOf<Producto>()
    private lateinit var database: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        val rvInventario: RecyclerView = view.findViewById(R.id.rvInventario)
        rvInventario.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProductoAdapter(listaProductos) { productoTocado ->
            val intent = Intent(requireContext(), FormularioActivity::class.java).apply {
                putExtra("ID", productoTocado.id)
                putExtra("NOMBRE", productoTocado.nombre)
                putExtra("CATEGORIA", productoTocado.categoria)
                putExtra("ESPECIFICACIONES", productoTocado.especificaciones)
                putExtra("CANTIDAD", productoTocado.cantidad)
                putExtra("PRECIO", productoTocado.precioPorUnidad)
                putExtra("FECHA", productoTocado.fechaCaducidad)
            }
            startActivity(intent)
        }
        rvInventario.adapter = adapter
        configurarDeslizarParaBorrar(rvInventario)

        // Animación de Bienvenida
        val bannerBienvenida = view.findViewById<View>(R.id.bannerBienvenida)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)
            withContext(Dispatchers.Main) {
                if (bannerBienvenida != null) {
                    bannerBienvenida.animate()
                        .alpha(0f)
                        .translationY(-bannerBienvenida.height.toFloat())
                        .setDuration(500)
                        .withEndAction { bannerBienvenida.visibility = View.GONE }
                        .start()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDesdeSQLite()
    }

    private fun cargarDatosDesdeSQLite() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val datosGuardados = database.productoDao().obtenerTodos()
                withContext(Dispatchers.Main) {
                    listaProductos.clear()
                    listaProductos.addAll(datosGuardados)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun configurarDeslizarParaBorrar(recyclerView: RecyclerView) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicion = viewHolder.adapterPosition
                val productoSeleccionado = listaProductos[posicion]

                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Producto")
                    .setMessage("¿Estás seguro de que deseas tirar '${productoSeleccionado.nombre}' de tu despensa?")
                    .setCancelable(false)
                    .setPositiveButton("Confirmar") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            database.productoDao().eliminar(productoSeleccionado)
                            withContext(Dispatchers.Main) {
                                listaProductos.removeAt(posicion)
                                adapter.notifyItemRemoved(posicion)
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
}