package com.example.teladecadastro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
    private var isPasswordVisible = false // Controle para alternar visibilidade da senha

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
        val btnVerSenha = findViewById<ImageView>(R.id.btnVerSenha)
        val btnLogar = findViewById<Button>(R.id.btnLogar)
        val txtCad = findViewById<Button>(R.id.txtCad)
        val buttonFrgt = findViewById<Button>(R.id.buttonFrgt)

        // Função para alternar visibilidade da senha
        btnVerSenha.setOnClickListener {
            if (isPasswordVisible) {
                // Ocultar senha
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnVerSenha.setImageResource(R.drawable.olho) // Ícone de olho fechado
            } else {
                // Mostrar senha
                editSenha.inputType = InputType.TYPE_CLASS_TEXT
                btnVerSenha.setImageResource(R.drawable.olho) // Pode mudar o ícone para "olho aberto", se necessário
            }

            // Move o cursor para o final do texto
            editSenha.setSelection(editSenha.text.length)

            isPasswordVisible = !isPasswordVisible // Alterna o estado
        }

        btnLogar.setOnClickListener {
            val email = editUsername.text?.toString()?.trim() ?: ""
            val senha = editSenha.text?.toString()?.trim() ?: ""

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            database.child("users").child(userId).get()
                                .addOnSuccessListener { dataSnapshot ->
                                    val username = dataSnapshot.child("name").value as? String ?: "usuário"
                                    val userType = dataSnapshot.child("userType").value as? String ?: ""

                                    if (userType.isEmpty()) {
                                        Toast.makeText(this, "Tipo de usuário não definido. Verifique o cadastro.", Toast.LENGTH_SHORT).show()
                                        Log.e("LOGIN", "Erro: userType está vazio para o usuário $userId")
                                        return@addOnSuccessListener
                                    }

                                    val intent = when (userType) {
                                        "Paciente" -> Intent(this, Paciente_tela::class.java)
                                        "Cuidador" -> Intent(this, Cuidador_tela::class.java)
                                        else -> {
                                            Toast.makeText(this, "Tipo de usuário desconhecido.", Toast.LENGTH_SHORT).show()
                                            Intent(this, MainActivity::class.java)
                                        }
                                    }

                                    intent.putExtra("USERNAME", username)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Log.e("LOGIN", "Erro ao recuperar dados do usuário: ${it.message}")
                                    Toast.makeText(this, "Erro ao recuperar dados do usuário.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        val exception = task.exception
                        if (exception != null) {
                            when (exception) {
                                is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {
                                    Toast.makeText(this, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(this, "Erro ao realizar login: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("LOGIN", "Erro ao realizar login: ${exception.message}")
                                }
                            }
                        }
                    }
                }
        }

        buttonFrgt.setOnClickListener {
            val email = editUsername.text?.toString()?.trim() ?: ""

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu email para redefinir a senha.", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email para redefinição de senha enviado.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro ao enviar email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.e("LOGIN", "Erro ao enviar email: ${task.exception?.message}")
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
