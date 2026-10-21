package com.example.recorridoapp.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Abrir un punto en Google Maps ----------
fun abrirEnGoogleMaps(context: Context, lat: Double, lng: Double, etiqueta: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(etiqueta)})")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val web = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, web))
    }
}

// ---------- Fecha y hora actual ----------
fun fechaActual(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

// ---------- Sensor de pasos (podómetro) ----------
@Composable
fun SensorPasos(activo: Boolean, onPasos: (Float) -> Unit) {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onPasos)

    DisposableEffect(activo) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                callback(event.values[0])
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (activo && sensor != null) {
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sm.unregisterListener(listener) }
    }
}

// ---------- Sensor de luz (modo claro / oscuro) ----------
@Composable
fun SensorLuz(onLuz: (Float) -> Unit) {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onLuz)

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                callback(event.values[0])
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) {
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sm.unregisterListener(listener) }
    }
}

// ---------- Ubicación en tiempo real ----------
@SuppressLint("MissingPermission")
@Composable
fun UbicacionEnVivo(activo: Boolean, onUbicacion: (Double, Double) -> Unit) {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onUbicacion)

    DisposableEffect(activo) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateDistanceMeters(2f)
            .build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { callback(it.latitude, it.longitude) }
            }
        }
        if (activo) {
            client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
        onDispose { client.removeLocationUpdates(locationCallback) }
    }
}