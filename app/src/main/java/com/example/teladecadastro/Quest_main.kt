package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Quest_main : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quest_main)

        database = FirebaseDatabase.getInstance().reference
        username = intent.getStringExtra("USERNAME") ?: ""

        val buttonEnviar = findViewById<Button>(R.id.txtCadastro)
        val problem1 = findViewById<EditText>(R.id.editP1)
        val problem2 = findViewById<EditText>(R.id.editP2)
        val problem3 = findViewById<EditText>(R.id.editP3)
        val problem4 = findViewById<EditText>(R.id.editP4)

        buttonEnviar.setOnClickListener {
            val problems = listOf(
                problem1.text.toString(),
                problem2.text.toString(),
                problem3.text.toString(),
                problem4.text.toString()
            )

            // Verificar se pelo menos um problema foi preenchido
            val atLeastOneProblem = problems.any { it.isNotEmpty() }

            if (atLeastOneProblem) {
                // Armazenar os problemas no Firebase Realtime Database
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let { uid ->
                    val userProblemsRef = database.child("users").child(uid).child("user_problems")
                    val problemsMap = mapOf(
                        "problem1" to problems[0],
                        "problem2" to problems[1],
                        "problem3" to problems[2],
                        "problem4" to problems[3]
                    )
                    userProblemsRef.setValue(problemsMap)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Problemas enviados com sucesso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Tela_Inicial::class.java)
                            intent.putExtra("USERNAME", username)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao enviar problemas. Tente novamente.", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(this, "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, informe pelo menos um problema.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
