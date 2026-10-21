package com.example.recorridoapp.data

import android.net.Uri

data class PuntoInteres(
    val uri: Uri,
    val lat: Double,
    val lng: Double,
    val fechaHora: String
)