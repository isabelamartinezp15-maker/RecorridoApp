package com.example.recorridoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recorridoapp.screens.MapaScreen
import com.example.recorridoapp.screens.PuntosScreen
import com.example.recorridoapp.ui.theme.RecorridoAppTheme
import com.example.recorridoapp.util.SensorLuz

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: RecorridoViewModel = viewModel()

            // Sensor de luz: cambia el modo claro/oscuro automáticamente
            SensorLuz { lux -> vm.actualizarModoOscuro(lux) }

            RecorridoAppTheme(darkTheme = vm.modoOscuro) {
                AppNavegacion(vm)
            }
        }
    }
}

@Composable
fun AppNavegacion(vm: RecorridoViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "mapa") {
        composable("mapa") {
            MapaScreen(vm = vm, onVerPuntos = { nav.navigate("puntos") })
        }
        composable("puntos") {
            PuntosScreen(viewModel = vm, onVolver = { nav.popBackStack() })
        }
    }
}