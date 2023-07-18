package com.example.firebaseauth

import com.google.firebase.firestore.GeoPoint

data class Traseu(
    var titlu: String?,
    var ruta: List<GeoPoint>?,
    var data : String?,
    var distanta: Int?
    )
