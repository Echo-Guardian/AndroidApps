package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Cadastro_main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro_main)

        val editCompleteName = findViewById<EditText>(R.id.editCompleteName)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editSenha = findViewById<EditText>(R.id.editSenha)
        val editSex = findViewById<EditText>(R.id.editSex)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val buttonCadastro = findViewById<Button>(R.id.txtCadastro)
        val buttonBack = findViewById<Button>(R.id.buttonBack)

        buttonCadastro.setOnClickListener {
            val completeName = editCompleteName.text.toString()
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()
            val sex = editSex.text.toString()

            if (completeName.isBlank() || email.isBlank() || senha.isBlank() || sex.isBlank()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Por favor, insira um email válido.", Toast.LENGTH_SHORT).show()
                } else {
                    if (senha.length < 6 || !senha.any { it.isUpperCase() }) {
                        Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres e uma letra maiúscula.", Toast.LENGTH_SHORT).show()
                    } else {
                        if (sex.uppercase() != "M" && sex.uppercase() != "F") {
                            Toast.makeText(this, "Por favor, insira 'M' para masculino ou 'F' para feminino.", Toast.LENGTH_SHORT).show()
                        } else {
                            val selectedId = radioGroup.checkedRadioButtonId
                            if (selectedId == R.id.radioPaciente) {
                                val intent = Intent(this, Quest_main::class.java)
                                intent.putExtra("USERNAME", completeName)
                                startActivity(intent)
                            } else if (selectedId == R.id.radioCuidador) {
                                val intent = Intent(this, Tela_Inicial::class.java)
                                intent.putExtra("USERNAME", completeName)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Por favor, selecione uma opção de cadastro.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
