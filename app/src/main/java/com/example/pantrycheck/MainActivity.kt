package com.example.pantrycheck

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var pestanaActual = R.id.nav_inventario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        val fabPrincipal: FloatingActionButton = findViewById(R.id.fabPrincipal)

        // Hacemos transparente el fondo para ver la curva del BottomAppBar
        bottomNav.background = null

        // Desactivamos el ícono "Fantasma" del centro (índice 2) para que no se pueda clickear
        bottomNav.menu.getItem(2).isEnabled = false

        // Iniciar con la pantalla de Inventario
        if (savedInstanceState == null) {
            cambiarFragmento(InventarioFragment())
        }

        // Control de navegación
        bottomNav.setOnItemSelectedListener { item ->
            pestanaActual = item.itemId
            when (item.itemId) {
                R.id.nav_inventario -> {
                    cambiarFragmento(InventarioFragment())
                    true
                }
                R.id.nav_compras -> {
                    cambiarFragmento(ComprasFragment())
                    true
                }
                R.id.nav_alertas -> {
                    Toast.makeText(this, "Módulo de Alertas en construcción", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_ajustes -> {
                    cambiarFragmento(AjustesFragment())
                    true
                }
                else -> false
            }
        }

        // Lógica del botón verde central
        fabPrincipal.setOnClickListener {
            if (pestanaActual == R.id.nav_inventario) {
                startActivity(Intent(this, FormularioActivity::class.java))
            } else if (pestanaActual == R.id.nav_compras) {
                // AHORA ABRE NUESTRA NUEVA PANTALLA COMPLETA
                startActivity(Intent(this, FormularioCompraActivity::class.java))
            } else {
                Toast.makeText(this, "Agrega elementos desde Inventario o Compras", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmentos, fragment)
            .commit()
    }
}