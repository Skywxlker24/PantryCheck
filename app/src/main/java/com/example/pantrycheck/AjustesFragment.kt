package com.example.pantrycheck

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class AjustesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etDiasNotificacion = view.findViewById<EditText>(R.id.etDiasNotificacion)
        val btnGuardarAjustes = view.findViewById<MaterialButton>(R.id.btnGuardarAjustes)
        val etNuevaCategoria = view.findViewById<EditText>(R.id.etNuevaCategoria)
        val btnAgregarCategoria = view.findViewById<MaterialButton>(R.id.btnAgregarCategoria)

        // 1. Cargar la configuración guardada anteriormente (Por defecto avisar 3 días antes)
        val sharedPref = requireActivity().getSharedPreferences("PantryAjustes", Context.MODE_PRIVATE)
        val diasGuardados = sharedPref.getInt("DiasAlerta", 3)
        etDiasNotificacion.setText(diasGuardados.toString())

        // 2. Guardar los nuevos ajustes cuando se presiona el botón verde
        btnGuardarAjustes.setOnClickListener {
            val nuevosDias = etDiasNotificacion.text.toString().toIntOrNull() ?: 3

            with (sharedPref.edit()) {
                putInt("DiasAlerta", nuevosDias)
                apply()
            }
            Toast.makeText(requireContext(), "Ajustes de notificaciones guardados", Toast.LENGTH_SHORT).show()
        }

        // 3. Simulación de agregar categoría maestra
        btnAgregarCategoria.setOnClickListener {
            val categoria = etNuevaCategoria.text.toString()
            if(categoria.isNotEmpty()){
                Toast.makeText(requireContext(), "Categoría '$categoria' lista para guardar en Catálogo", Toast.LENGTH_SHORT).show()
                etNuevaCategoria.text.clear()
            }
        }
    }
}