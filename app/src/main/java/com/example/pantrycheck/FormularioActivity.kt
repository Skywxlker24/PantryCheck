package com.example.pantrycheck

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FormularioActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var idProductoEditar: Int = -1
    private var fechaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        database = AppDatabase.getDatabase(this)

        // Conectamos todos los elementos visuales
        val btnVolver = findViewById<TextView>(R.id.btnVolverF) // NUEVO BOTÓN
        val tvTitulo = findViewById<TextView>(R.id.tvTituloFormulario)
        val etNombre = findViewById<EditText>(R.id.etNombreF)
        val etCategoria = findViewById<EditText>(R.id.etCategoriaF)
        val etEspecificaciones = findViewById<EditText>(R.id.etEspecificacionesF)
        val etCantidad = findViewById<EditText>(R.id.etCantidadF)
        val etPrecio = findViewById<EditText>(R.id.etPrecioF)
        val btnFecha = findViewById<Button>(R.id.btnSeleccionarFechaF)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarF)

        // LÓGICA DEL BOTÓN VOLVER
        btnVolver.setOnClickListener {
            finish() // Esto cierra la pantalla de inmediato y te regresa al Inventario
        }

        // Verificamos si venimos de "Editar"
        idProductoEditar = intent.getIntExtra("ID", -1)

        if (idProductoEditar != -1) {
            tvTitulo.text = "Editar Producto"
            etNombre.setText(intent.getStringExtra("NOMBRE"))
            etCategoria.setText(intent.getStringExtra("CATEGORIA"))
            etEspecificaciones.setText(intent.getStringExtra("ESPECIFICACIONES"))
            etCantidad.setText(intent.getIntExtra("CANTIDAD", 1).toString())
            etPrecio.setText(intent.getDoubleExtra("PRECIO", 0.0).toString())

            fechaSeleccionada = intent.getStringExtra("FECHA") ?: ""
            btnFecha.text = "Vence: $fechaSeleccionada"
        }

        // Calendario
        btnFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
                btnFecha.text = "Vence: $fechaSeleccionada"
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        // Guardar
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val categoria = etCategoria.text.toString().ifBlank { "Sin Categoría" }
            val esp = etEspecificaciones.text.toString().ifBlank { "N/A" }
            val cantidadStr = etCantidad.text.toString()
            val precioStr = etPrecio.text.toString()

            if (nombre.isNotBlank() && cantidadStr.isNotBlank() && precioStr.isNotBlank() && fechaSeleccionada.isNotBlank()) {
                val cantidad = cantidadStr.toIntOrNull() ?: 1
                val precio = precioStr.replace(",", ".").toDoubleOrNull() ?: 0.0

                val productoFinal = Producto(
                    id = if (idProductoEditar != -1) idProductoEditar else 0,
                    nombre = nombre,
                    especificaciones = esp,
                    categoria = categoria,
                    cantidad = cantidad,
                    precioPorUnidad = precio,
                    fechaCaducidad = fechaSeleccionada
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    if (idProductoEditar == -1) {
                        database.productoDao().insertar(productoFinal)
                    } else {
                        database.productoDao().actualizar(productoFinal)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormularioActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        finish() // Cierra al guardar
                    }
                }
            } else {
                Toast.makeText(this, "Faltan datos o la fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }
}