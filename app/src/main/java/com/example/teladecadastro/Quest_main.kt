package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Quest_main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quest_main)

        val username = intent.getStringExtra("USERNAME")

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
            val atLeastOneProblem = problems.any { it.isNotEmpty() }

            if (atLeastOneProblem) {
                val intent = Intent(this, Tela_Inicial::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, informe pelo menos um problema.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
