package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val editUsername = findViewById<EditText>(R.id.editUsername)
        val editSenha = findViewById<EditText>(R.id.editSenha)
        val btnLogar = findViewById<Button>(R.id.btnLogar)
        val txtCad = findViewById<Button>(R.id.txtCad)

        btnLogar.setOnClickListener {
            val email = editUsername.text.toString()
            val senha = editSenha.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val username = user?.displayName ?: "usuário"

                            Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Tela_Inicial::class.java)
                            intent.putExtra("USERNAME", username)
                            startActivity(intent)
                            finish()
                        } else {
                            val exception = task.exception
                            if (exception != null) {
                                when (exception) {
                                    is IllegalArgumentException -> {
                                        Toast.makeText(this, "Formato de email inválido.", Toast.LENGTH_SHORT).show()
                                    }
                                    is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {
                                        Toast.makeText(this, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        Toast.makeText(this, "Erro ao realizar login: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
            }
        }


        txtCad.setOnClickListener {
            val intent = Intent(this, Cadastro_main::class.java)
            startActivity(intent)
        }
    }
}
