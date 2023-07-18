package com.example.firebaseauth

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InformatiiActivity : AppCompatActivity() {
    private lateinit var distantaValoare : TextView
    private lateinit var durataValoare: TextView
    private lateinit var vitezaValoare: TextView
    private val datastore = FirebaseFirestore.getInstance()
    private var distantaTotala: Int = 0
    private var vitezaMedie: Int = 0
    private var sumaDurate = 0
    private var numarCurse = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informatii)

        distantaValoare = findViewById(R.id.distanta_textview_valoare)
        durataValoare = findViewById(R.id.durata_textview_valoare)
        vitezaValoare = findViewById(R.id.viteza_textview_valoare)

        val user_curent = FirebaseAuth.getInstance().currentUser
        if (user_curent != null) {
            val colectieCurse = datastore.collection("users").document(user_curent.uid).collection("curse")
            colectieCurse.get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val distanta = document.getLong("distanta")?.toInt()
                        distantaTotala += distanta!!
                        val viteza = document.getLong("viteza medie")?.toInt()
                        vitezaMedie += viteza!!
                        numarCurse++
                        val durata = document.getString("durata")
                        val durataSplit = durata?.split(":") ?: listOf("0", "0", "0")
                        val ore = durataSplit[0].toInt()
                        val minute = durataSplit[1].toInt()
                        val secunde = durataSplit[2].toInt()
                        val durataTotalaSecunde = ore * 3600 + minute * 60 + secunde
                        sumaDurate += durataTotalaSecunde
                    }
                    distantaValoare.text = distantaTotala.toString() + " m"
                    val oreSuma = sumaDurate / 3600
                    val minuteSuma = (sumaDurate % 3600) / 60
                    val secundeSuma = sumaDurate % 60
                    val durataTotalaString = String.format("%02d:%02d:%02d", oreSuma, minuteSuma, secundeSuma)
                    durataValoare.text = durataTotalaString
                    val mediaVitezei = if (numarCurse > 0) vitezaMedie.toFloat() / numarCurse else 0f
                    vitezaValoare.text = mediaVitezei.toString() + " KM/H"
                }
                .addOnFailureListener {
                    Toast.makeText(this@InformatiiActivity, "Eroare", Toast.LENGTH_LONG)
                        .show()
                }
        }
    }
}