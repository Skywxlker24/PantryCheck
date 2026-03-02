package com.example.pantrycheck

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FormularioActivity : AppCompatActivity() {

    private var productoId: Int = 0 // 0 significa que es un producto nuevo
    private var fechaSeleccionada: String = ""
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        // Iniciamos la conexión con tu base de datos local SQLite
        database = AppDatabase.getDatabase(this)

        // Enlazamos la lógica con el diseño visual (IDs exactos)
        val btnVolver = findViewById<TextView>(R.id.btnVolverF)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloFormulario)
        val btnEscaner = findViewById<MaterialButton>(R.id.btnEscanerFormulario)

        val etNombre = findViewById<EditText>(R.id.etNombreF)
        val etCategoria = findViewById<EditText>(R.id.etCategoriaF)
        val etEspecificaciones = findViewById<EditText>(R.id.etEspecificacionesF)
        val etCantidad = findViewById<EditText>(R.id.etCantidadF)
        val etPrecio = findViewById<EditText>(R.id.etPrecioF)

        val btnFecha = findViewById<MaterialButton>(R.id.btnSeleccionarFechaF)
        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarF)

        // 1. Botón de regresar
        btnVolver.setOnClickListener { finish() }

        // 2. Botón del escáner (Simulación de Auto-llenado)
        btnEscaner.setOnClickListener {
            Toast.makeText(this, "Escaneando código...", Toast.LENGTH_SHORT).show()
            etNombre.setText("Cereal Kelloggs")
            etCategoria.setText("Despensa")
            etEspecificaciones.setText("Caja 700g")
        }

        // 3. Botón para abrir el Calendario (Selector de Fecha)
        btnFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                // Formateamos el mes y día a 2 dígitos para que no falle tu semáforo de colores
                val mesFormateado = String.format("%02d", monthOfYear + 1)
                val diaFormateado = String.format("%02d", dayOfMonth)
                fechaSeleccionada = "$diaFormateado/$mesFormateado/$year"

                // Actualizamos el texto del botón azul
                btnFecha.text = fechaSeleccionada
            }, anio, mes, dia)

            datePickerDialog.show()
        }

        // 4. ¿Estamos editando un producto o creando uno nuevo?
        val intent = intent
        if (intent.hasExtra("ID")) {
            productoId = intent.getIntExtra("ID", 0)
            tvTitulo.text = "Editar Producto"
            btnGuardar.text = "Actualizar Producto"

            etNombre.setText(intent.getStringExtra("NOMBRE"))
            etCategoria.setText(intent.getStringExtra("CATEGORIA"))
            etEspecificaciones.setText(intent.getStringExtra("ESPECIFICACIONES"))
            etCantidad.setText(intent.getIntExtra("CANTIDAD", 1).toString())
            etPrecio.setText(intent.getDoubleExtra("PRECIO", 0.0).toString())

            fechaSeleccionada = intent.getStringExtra("FECHA") ?: ""
            if (fechaSeleccionada.isNotEmpty()) {
                btnFecha.text = fechaSeleccionada
            }
        }

        // 5. Botón Guardar / Actualizar
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val categoria = etCategoria.text.toString()
            val especificaciones = etEspecificaciones.text.toString()
            val cantidadStr = etCantidad.text.toString()
            val precioStr = etPrecio.text.toString()

            // Validamos que los campos obligatorios no estén vacíos
            if (nombre.isNotBlank() && cantidadStr.isNotBlank() && precioStr.isNotBlank() && fechaSeleccionada.isNotBlank()) {
                val cantidad = cantidadStr.toIntOrNull() ?: 1
                val precio = precioStr.toDoubleOrNull() ?: 0.0

                // Creamos el objeto Producto
                val producto = Producto(
                    id = productoId, // Si es 0, significa que es un nuevo producto, si no, es un producto existente
                    nombre = nombre,
                    categoria = categoria,
                    especificaciones = especificaciones,
                    cantidad = cantidad,
                    precioPorUnidad = precio,
                    fechaCaducidad = fechaSeleccionada
                )

                // Guardamos en la base de datos en segundo plano
                lifecycleScope.launch(Dispatchers.IO) {
                    if (productoId == 0) {
                        database.productoDao().insertar(producto)
                    } else {
                        database.productoDao().actualizar(producto)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormularioActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        finish() // Cierra la pantalla y regresa al inventario
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos y elige una fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }
}