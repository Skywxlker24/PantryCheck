package com.example.pantrycheck

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FormularioCompraActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var compraId: Int = 0 // 0 significa que es nuevo. Si cambia, significa que estamos editando.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_compra)

        database = AppDatabase.getDatabase(this)

        val btnVolver = findViewById<TextView>(R.id.btnVolverC)
        val etNombre = findViewById<EditText>(R.id.etNombreC)
        val etCantidad = findViewById<EditText>(R.id.etCantidadC)
        val btnSimular = findViewById<MaterialButton>(R.id.btnSimularC)

        btnVolver.setOnClickListener { finish() }

        // Logica edición
        if (intent.hasExtra("ID")) {
            compraId = intent.getIntExtra("ID", 0)
            etNombre.setText(intent.getStringExtra("NOMBRE"))
            etCantidad.setText(intent.getIntExtra("CANTIDAD", 1).toString())
            btnSimular.text = "Actualizar Precio"
        }

        btnSimular.setOnClickListener {
            val nombre = etNombre.text.toString()
            val cantStr = etCantidad.text.toString()

            if (nombre.isNotBlank() && cantStr.isNotBlank()) {
                val cantidad = cantStr.toIntOrNull() ?: 1
                simularPreciosYGuardar(nombre, cantidad)
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simularPreciosYGuardar(nombre: String, cantidad: Int) {
        val precioBase = 15.0 + (Math.random() * 50)
        val precioWalmart = String.format(Locale.US, "%.2f", precioBase).toDouble()
        val precioSoriana = String.format(Locale.US, "%.2f", precioBase + (Math.random() * 5)).toDouble()
        val precioHEB = String.format(Locale.US, "%.2f", precioBase + (Math.random() * 8)).toDouble()

        val precios = mapOf("Walmart" to precioWalmart, "Soriana" to precioSoriana, "HEB" to precioHEB)
        val mejorTienda = precios.minByOrNull { it.value }

        val mensaje = "Buscando en la red...\n" +
                "🏪 Soriana: \$${precioSoriana}\n" +
                "🏪 HEB: \$${precioHEB}\n" +
                "🏪 Walmart: \$${precioWalmart}\n\n" +
                "✅ La mejor opción es ${mejorTienda?.key}.\n" +
                "¿Guardar en la lista por \$${mejorTienda?.value}?"

        AlertDialog.Builder(this)
            .setTitle("Simulador de Ahorro")
            .setMessage(mensaje)
            .setPositiveButton("Sí, guardar") { _, _ ->
                // Mantenemos el ID: Si es 0 es nuevo, si no, es edición
                val nuevoItem = ItemCompra(
                    id = compraId,
                    nombre = nombre,
                    cantidad = cantidad,
                    precioEstimado = mejorTienda?.value ?: 0.0
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    if (compraId == 0) {
                        database.compraDao().insertar(nuevoItem)
                    } else {
                        database.compraDao().actualizar(nuevoItem)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormularioCompraActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}