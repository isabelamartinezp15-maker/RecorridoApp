package com.example.recorridoapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.recorridoapp.data.PuntoInteres
import com.google.android.gms.maps.model.LatLng

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

    // Ruta recorrida (para la Polyline opcional)
    val ruta = mutableStateListOf<LatLng>()

    // Valor inicial del contador de pasos (el sensor es acumulado)
    private var pasosBase: Float? = null

    fun iniciarRecorrido() {
        enRecorrido = true
        pasos = 0
        distanciaMetros = 0.0
        pasosBase = null
        ruta.clear()
    }

    fun terminarRecorrido() {
        enRecorrido = false
    }

    fun actualizarUbicacion(lat: Double, lng: Double) {
        latActual = lat
        lngActual = lng
        if (enRecorrido) ruta.add(LatLng(lat, lng))
    }

    fun actualizarModoOscuro(lux: Float) {
        // Entre 20 y 40 no cambia nada (evita que parpadee)
        if (lux < 20f) modoOscuro = true
        else if (lux > 40f) modoOscuro = false
    }

    fun actualizarPasos(totalSensor: Float) {
        if (!enRecorrido) return
        val base = pasosBase ?: totalSensor.also { pasosBase = it }
        pasos = (totalSensor - base).toInt()
        distanciaMetros = pasos * 0.75
    }

    fun agregarPunto(punto: PuntoInteres) {
        puntos.add(0, punto) // el más reciente primero
    }
}