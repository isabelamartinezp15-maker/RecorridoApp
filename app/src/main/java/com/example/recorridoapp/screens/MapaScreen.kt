package com.example.recorridoapp.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.recorridoapp.RecorridoViewModel
import com.example.recorridoapp.data.PuntoInteres
import com.example.recorridoapp.util.SensorPasos
import com.example.recorridoapp.util.UbicacionEnVivo
import com.example.recorridoapp.util.fechaActual
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.io.File

@Composable
fun MapaScreen(
    vm: RecorridoViewModel,
    onVerPuntos: () -> Unit
) {
    val context = LocalContext.current
    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // ---- Permisos (GPS + cámara) ----
    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val gps = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val cam = res[Manifest.permission.CAMERA] == true
        if (gps && cam) {
            vm.iniciarRecorrido()
        } else {
            Toast.makeText(
                context,
                "Se necesitan los permisos de GPS y cámara para iniciar el recorrido",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ---- Cámara ----
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = fotoUri
        val lat = vm.latActual
        val lng = vm.lngActual
        if (ok && uri != null && lat != null && lng != null) {
            vm.agregarPunto(
                PuntoInteres(uri = uri, lat = lat, lng = lng, fechaHora = fechaActual())
            )
            Toast.makeText(context, "Punto de interés guardado", Toast.LENGTH_SHORT).show()
        } else if (ok) {
            Toast.makeText(context, "Aún no hay ubicación, intenta de nuevo", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- Sensores y ubicación (solo con recorrido activo) ----
    SensorPasos(activo = vm.enRecorrido) { vm.actualizarPasos(it) }
    UbicacionEnVivo(activo = vm.enRecorrido) { lat, lng -> vm.actualizarUbicacion(lat, lng) }

    // ---- Mapa ----
    val cameraState = rememberCameraPositionState()
    LaunchedEffect(vm.latActual, vm.lngActual) {
        val lat = vm.latActual
        val lng = vm.lngActual
        if (lat != null && lng != null) {
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 17f))
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = vm.enRecorrido),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Un marcador por cada foto
            vm.puntos.forEach { punto ->
                key(punto.id) {
                    Marker(
                        state = rememberMarkerState(position = LatLng(punto.lat, punto.lng)),
                        title = "Punto de interés",
                        snippet = punto.fechaHora
                    )
                }
            }
            // Ruta recorrida
            if (vm.ruta.size > 1) {
                Polyline(points = vm.ruta.toList(), color = Color(0xFF1E88E5), width = 12f)
            }
        }

        // Tarjeta de estadísticas
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("👣 ${vm.pasos} pasos", style = MaterialTheme.typography.titleMedium)
                Text(
                    "📏 " + String.format("%.1f m", vm.distanciaMetros),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Botones
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!vm.enRecorrido) {
                Button(
                    onClick = {
                        permisosLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACTIVITY_RECOGNITION
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Iniciar Recorrido") }
            } else {
                Button(
                    onClick = {
                        val file = File.createTempFile("foto_", ".jpg", context.cacheDir)
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", file
                        )
                        fotoUri = uri
                        camaraLauncher.launch(uri)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("📷 Tomar foto") }

                OutlinedButton(
                    onClick = { vm.terminarRecorrido() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Terminar recorrido") }
            }

            OutlinedButton(
                onClick = onVerPuntos,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Puntos de Interés (${vm.puntos.size})") }
        }
    }
}