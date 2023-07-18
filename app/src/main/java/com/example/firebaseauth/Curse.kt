package com.example.firebaseauth
import com.google.firebase.firestore.GeoPoint
data class Curse (
    var distanta : Int?,
    var viteza : Int?,
    var data: String?,
    var traseu: List<GeoPoint>?,
    var durata : String?
    )