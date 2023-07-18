package com.example.firebaseauth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ObiectiveActivity : AppCompatActivity() {
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButonDistanta: RadioButton
    private lateinit var radioButonDurata: RadioButton
    private lateinit var butonGata: Button
    private lateinit var valoareObiectiv: EditText
    private lateinit var valoare: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obiective)

        radioGroup = findViewById(R.id.radioGroup)
        radioButonDistanta = findViewById(R.id.radioButtonDistanta)
        radioButonDurata = findViewById(R.id.radioButtonDurata)
        butonGata = findViewById(R.id.butonGata)
        valoareObiectiv = findViewById(R.id.valoare_obiectiv)

        butonGata.setOnClickListener{
            valoare = valoareObiectiv.text.toString()
            if(valoareObiectiv.text.isNotEmpty()) {
                val optiuneSelectataId = radioGroup.checkedRadioButtonId
                if (optiuneSelectataId != -1) {
                    val butonSelectat = findViewById<RadioButton>(optiuneSelectataId)
                    val textSelectat = butonSelectat.text.toString()
                    val bundle = Bundle()
                    if (textSelectat == "Distanta (metri)")
                        bundle.putString("distanta_obiectiv", valoare)
                    else
                        bundle.putString("durata_obiectiv", valoare)
                    val intent = Intent(this, MapActivity::class.java)
                    intent.putExtra("obiectiv", bundle)
                    startActivity(intent)
                }
            }
            else
                Toast.makeText(this, "Câmpul pentru valoarea obiectivului trebuie completat!", Toast.LENGTH_SHORT).show()
        }
    }
}