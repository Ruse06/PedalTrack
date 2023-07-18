package com.example.firebaseauth

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class CurseSalvateAdapter(private val listaTraseu: kotlin.collections.MutableList<Traseu>) : RecyclerView.Adapter<CurseSalvateAdapter.MyViewHolder>() {

    private var itemHeight: Int = 0
    private val datastore = FirebaseFirestore.getInstance()
    fun setItemHeight(height: Int) {
        itemHeight = height
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurseSalvateAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trasee_item, parent, false)
        return CurseSalvateAdapter.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CurseSalvateAdapter.MyViewHolder, positie: Int) {
        val item_curent = listaTraseu[positie]
        holder.itemView.layoutParams.height = itemHeight
        holder.titlu.text = "Titlu: " + item_curent.titlu.toString()
        holder.data.text = "Data: " + item_curent.data.toString()
        holder.distanta.text = "Distanta planificata: " + item_curent.distanta + "m"
        holder.delete_buton.setOnClickListener {
            val mesajAlerta = AlertDialog.Builder(holder.itemView.context)
            mesajAlerta.setTitle("Doriți să ștergeți planificarea?")
            mesajAlerta.setPositiveButton("Da") { _, _ ->
                val cursa = listaTraseu[positie]
                deleteItem(cursa)
            }
            mesajAlerta.setNegativeButton("Nu") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = mesajAlerta.create()
            dialog.show()
        }
        holder.open_buton.setOnClickListener {
            val traseuLatLng = ArrayList<LatLng>()
            for (punct in item_curent.ruta!!)
                traseuLatLng.add(LatLng(punct.latitude, punct.longitude))
            val context = holder.itemView.context
            val intent = Intent(context, MapActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList("ruta", traseuLatLng)
            bundle.putInt("distanta", item_curent.distanta!!)
            bundle.putString("titlu", item_curent.titlu!!)
            bundle.putString("data", item_curent.data!!)
            intent.putExtra("traseu_planificat", bundle)
            context.startActivity(intent)
        }
    }

    fun deleteItem(traseu: Traseu) {
        val user_curent = FirebaseAuth.getInstance().currentUser
        if (user_curent != null) {
            val colectieCurse = datastore.collection("users")
                .document(user_curent.uid)
                .collection("trasee")
            colectieCurse.whereEqualTo("data", traseu.data)
                .whereEqualTo("distanta", traseu.distanta)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        document.reference.delete()
                    }
                }
        }
        listaTraseu.remove(traseu)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var titlu : TextView = itemView.findViewById(R.id.titlu_item)
        var data : TextView = itemView.findViewById(R.id.data_traseu)
        var distanta : TextView = itemView.findViewById(R.id.distanta_item)
        var delete_buton : Button = itemView.findViewById(R.id.delete_buton_traseu)
        var open_buton : Button = itemView.findViewById(R.id.open_buton_traseu)
    }

    override fun getItemCount(): Int {
        return listaTraseu.size
    }
}