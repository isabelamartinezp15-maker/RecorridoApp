package com.example.recorridoapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recorridoapp.RecorridoViewModel
import com.example.recorridoapp.data.PuntoInteres
import com.example.recorridoapp.util.abrirEnGoogleMaps

@Composable
fun PuntosScreen(
    viewModel: RecorridoViewModel,
    onVolver: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            Text("Puntos de Interés", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            // Resumen tipo dashboard
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DatoResumen("Fotos", viewModel.puntos.size.toString())
                    DatoResumen("Pasos", viewModel.pasos.toString())
                    DatoResumen("Distancia", String.format("%.0f m", viewModel.distanciaMetros))
                }
            }

            Spacer(Modifier.height(12.dp))

            if (viewModel.puntos.isEmpty()) {
                Text(
                    "Aún no hay puntos de interés. ¡Toma una foto durante el recorrido!",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(viewModel.puntos, key = { it.id }) { punto ->
                        PuntoCard(punto)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al mapa")
            }
        }
    }
}

@Composable
private fun DatoResumen(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleLarge)
        Text(titulo, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PuntoCard(punto: PuntoInteres) {
    val context = LocalContext.current
    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = punto.uri,
                contentDescription = "Foto del punto de interés",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Spacer(Modifier.height(4.dp))
            Text(punto.fechaHora, style = MaterialTheme.typography.labelMedium)
            Text(
                String.format("%.5f, %.5f", punto.lat, punto.lng),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = { abrirEnGoogleMaps(context, punto.lat, punto.lng, "Punto de interés") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver en Maps")
            }
        }
    }
}