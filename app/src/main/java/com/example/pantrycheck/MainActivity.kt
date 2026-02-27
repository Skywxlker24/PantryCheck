package com.example.pantrycheck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)

        // Al abrir la app, cargamos el Inventario por defecto
        if (savedInstanceState == null) {
            cambiarFragmento(InventarioFragment())
        }

        // Escuchamos cuando tocan un botón del menú inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inventario -> {
                    cambiarFragmento(InventarioFragment())
                    true
                }
                R.id.nav_compras -> {
                    cambiarFragmento(ComprasFragment())
                    true
                }
                R.id.nav_ajustes -> {
                    cambiarFragmento(AjustesFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Función mágica que intercambia los fragmentos en la pantalla
    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmentos, fragment)
            .commit()
    }
}