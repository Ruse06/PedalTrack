package com.example.firebaseauth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class IstoricActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val datastore = FirebaseFirestore.getInstance()
    private lateinit var bottomNavigationView: BottomNavigationView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        bottomNavigationView = findViewById(R.id.map_bot_navigation_menu_history)
        bottomNavigationView.clearFocus()
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.menu_history -> {
                    val intent = Intent(this, IstoricActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.trasee_salvate -> {
                    val intent = Intent(this, CurseSalvateActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        recyclerView = findViewById(R.id.history_rv)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val listaCurse = mutableListOf<Curse>()
        val adapter = IstoricAdapter(listaCurse)
        recyclerView.adapter = adapter

        val displayMetrics = resources.displayMetrics
        val nrItemeVizibile = 3
        val itemInaltime = displayMetrics.heightPixels / nrItemeVizibile

        adapter.setItemHeight(itemInaltime)
        adapter.notifyDataSetChanged()

        val user_curent = FirebaseAuth.getInstance().currentUser
        if (user_curent != null) {
            val colectieCurse = datastore.collection("users").document(user_curent.uid).collection("curse")
            colectieCurse.get()
                .addOnSuccessListener { querySnapshot ->
                    listaCurse.clear()
                    for (document in querySnapshot){
                        val distanta = document.getLong("distanta")?.toInt()
                        val viteza = document.getLong("viteza medie")?.toInt()
                        val durata = document.getString("durata")
                        val data = document.getString("data")
                        val ruta = document.get("traseu") as? List<GeoPoint>
                        val cursa = Curse(distanta, viteza, data, ruta, durata)
                        listaCurse.add(cursa)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener{
                    Toast.makeText(this@IstoricActivity, "Nu stiu ce are :(", Toast.LENGTH_LONG).show()
                }
        }
    }
}