package com.example.firebaseauth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class SignInActivity: AppCompatActivity() {

    private lateinit var signInButton: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var emailString: String
    private lateinit var parolaString: String
    private lateinit var auth: FirebaseAuth
    private lateinit var parolaUitata : TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)


        setContentView(R.layout.activity_signin)


        auth = Firebase.auth
        editTextEmail = findViewById(R.id.emailEditText)
        editTextPassword = findViewById(R.id.editTextParola)
        signInButton = findViewById(R.id.signinButton)
        parolaUitata = findViewById(R.id.parolaUitataTextView)

        signInButton.setOnClickListener {
            emailString = editTextEmail.text.toString().trim()
            parolaString = editTextPassword.text.toString().trim()
            if (emailString.isEmpty() || parolaString.isEmpty()) {
                Toast.makeText(this, "Trebuie sa completati campurile.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailString, parolaString)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Autentificare reusita!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MapActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(this, "Adresa de email invalida.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Parola invalida.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Login-ul a esuat.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

        parolaUitata.setOnClickListener {
            emailString = editTextEmail.text.toString().trim()
            if (emailString.isEmpty()) {
                Toast.makeText(this, "Va rog introduceti adresa de email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(emailString)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email-ul pentru resetarea parolei a fost trimis.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Nu s-a putut trimite email-ul pentru resetarea parolei.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}