package com.example.teladecadastro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val editUsername = findViewById<EditText>(R.id.editUsername)
        val editSenha = findViewById<EditText>(R.id.editSenha)
        val btnLogar = findViewById<Button>(R.id.btnLogar)
        val txtCad = findViewById<Button>(R.id.txtCad)
        val buttonFrgt = findViewById<Button>(R.id.buttonFrgt)

        btnLogar.setOnClickListener {
            val email = editUsername.text.toString()
            val senha = editSenha.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                database.child("users").child(userId).get().addOnSuccessListener { dataSnapshot ->
                                    val username = dataSnapshot.child("name").value as? String ?: "usuário"
                                    val userType = dataSnapshot.child("userType").value as? String ?: ""

                                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()

                                    val intent = when (userType) {
                                        "Paciente" -> Intent(this, Paciente_tela::class.java)
                                        "Cuidador" -> Intent(this, Cuidador_tela::class.java)
                                        else -> {
                                            Toast.makeText(this, "Tipo de usuário desconhecido.", Toast.LENGTH_SHORT).show()
                                            Intent(this, MainActivity::class.java) // Ou alguma tela padrão
                                        }
                                    }

                                    intent.putExtra("USERNAME", username)
                                    startActivity(intent)
                                    finish()
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Erro ao recuperar dados do usuário.", Toast.LENGTH_SHORT).show()
                                }
                            }
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


        buttonFrgt.setOnClickListener {
            val email = editUsername.text.toString()

            if (email.isBlank()) {
                Toast.makeText(this, "Por favor, insira seu email para redefinir a senha.", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email para redefinição de senha enviado.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro ao enviar email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
