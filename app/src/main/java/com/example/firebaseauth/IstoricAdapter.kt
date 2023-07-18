package com.example.firebaseauth
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IstoricAdapter ( private val listaCurse: kotlin.collections.MutableList<Curse>) : RecyclerView.Adapter<IstoricAdapter.MyViewHolder>(){

    private var itemHeight: Int = 0
    private val datastore = FirebaseFirestore.getInstance()
    fun setItemHeight(height: Int) {
        itemHeight = height
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, positie: Int) {
        val item_curent = listaCurse[positie]
        holder.itemView.layoutParams.height = itemHeight
        holder.data.text = "Data: " + item_curent.data.toString()
        holder.distanta.text = "Distanta: " + item_curent.distanta.toString() + " m"
        holder.viteza.text = "Viteza medie: " + item_curent.viteza.toString() + " KM/H"
        holder.durata.text = "Durata: " + item_curent.durata
        holder.delete_buton.setOnClickListener {
            val mesajAlerta = AlertDialog.Builder(holder.itemView.context)
            mesajAlerta.setTitle("Doriți să ștergeți cursa?")
            mesajAlerta.setPositiveButton("Da") { _, _ ->
                val cursa = listaCurse[positie]
                deleteItem(cursa)
            }
            mesajAlerta.setNegativeButton("Nu") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = mesajAlerta.create()
            dialog.show()
        }


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MapActivity::class.java)
            val traseuLatLng = ArrayList<LatLng>()
            for (punct in item_curent.traseu!!)
                traseuLatLng.add(LatLng(punct.latitude, punct.longitude))
            val bundle = Bundle()
            bundle.putParcelableArrayList("traseu", traseuLatLng)
            bundle.putInt("distanta", item_curent.distanta!!)
            bundle.putInt("viteza", item_curent.viteza!!)
            bundle.putString("durata", item_curent.durata!!)
            intent.putExtra("bundle", bundle)
            context.startActivity(intent)
        }

        holder.distribuie_buton.setOnClickListener{
            val context = holder.itemView.context

            val message = "Am terminat o cursă folosind PedalTrack: " +
                    "Distanta: ${item_curent.distanta} m, " +
                    "Viteza medie: ${item_curent.viteza} KM/H, " +
                    "Durata: ${item_curent.durata}."

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return listaCurse.size
    }

    fun deleteItem(cursa: Curse) {
        val user_curent = FirebaseAuth.getInstance().currentUser
        if (user_curent != null) {
            val colectieCurse = datastore.collection("users")
                .document(user_curent.uid)
                .collection("curse")

            colectieCurse.whereEqualTo("data", cursa.data)
                .whereEqualTo("distanta", cursa.distanta)
                .whereEqualTo("viteza medie", cursa.viteza)
                .whereEqualTo("durata", cursa.durata)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        document.reference.delete()
                    }
                }
        }
        listaCurse.remove(cursa)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var distanta : TextView = itemView.findViewById(R.id.distanta_item)
        var viteza : TextView = itemView.findViewById(R.id.viteza_item)
        var data : TextView = itemView.findViewById(R.id.data_item)
        var delete_buton : Button = itemView.findViewById(R.id.delete_buton)
        var durata : TextView = itemView.findViewById(R.id.durata_item)
        var distribuie_buton : Button = itemView.findViewById(R.id.share_button)
    }
}
