package com.example.recorridoapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.recorridoapp.data.PuntoInteres

class RecorridoViewModel : ViewModel() {

    // Estado del recorrido
    var enRecorrido by mutableStateOf(false)
        private set
    var pasos by mutableStateOf(0)
        private set
    var distanciaMetros by mutableStateOf(0.0)
        private set

    // Última ubicación conocida (null si aún no hay)
    var latActual by mutableStateOf<Double?>(null)
        private set
    var lngActual by mutableStateOf<Double?>(null)
        private set

    // Sensor de luz
    var modoOscuro by mutableStateOf(false)
        private set

    // Fotos tomadas
    val puntos = mutableStateListOf<PuntoInteres>()

    // Valor inicial del contador de pasos (el sensor es acumulado)
    private var pasosBase: Float? = null

    fun iniciarRecorrido() {
        enRecorrido = true
        pasos = 0
        distanciaMetros = 0.0
        pasosBase = null
    }

    fun terminarRecorrido() {
        enRecorrido = false
    }

    fun actualizarUbicacion(lat: Double, lng: Double) {
        latActual = lat
        lngActual = lng
    }

    fun actualizarModoOscuro(lux: Float) {
        // TODO 1: si lux < 20 -> modo oscuro; si lux > 40 -> modo claro.
        // Entre 20 y 40 no cambies nada (evita que parpadee).
    }

    fun actualizarPasos(totalSensor: Float) {
        // TODO 2: guarda el primer valor en pasosBase,
        // calcula pasos = totalSensor - pasosBase
        // y distanciaMetros = pasos * 0.75
    }

    fun agregarPunto(punto: PuntoInteres) {
        puntos.add(0, punto) // el más reciente primero
    }
}