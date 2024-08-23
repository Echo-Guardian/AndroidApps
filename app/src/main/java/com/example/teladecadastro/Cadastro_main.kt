package com.example.teladecadastro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

data class User(val name: String, val email: String, val password: String, val sex: String, val userType: String)

class Cadastro_main : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro_main)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

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
            Log.d("Cadastro", "Nome: $completeName, Email: $email, Senha: $senha, Sexo: $sex")

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
                            val userType = when (selectedId) {
                                R.id.radioPaciente -> "Paciente"
                                R.id.radioCuidador -> "Cuidador"
                                else -> ""
                            }

                            if (userType.isEmpty()) {
                                Toast.makeText(this, "Por favor, selecione uma opção de cadastro.", Toast.LENGTH_SHORT).show()
                            } else {
                                auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val result = task.result?.signInMethods ?: emptyList()
                                        if (result.isNotEmpty()) {
                                            Toast.makeText(this, "O email informado já está em uso.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            auth.createUserWithEmailAndPassword(email, senha)
                                                .addOnCompleteListener { createUserTask ->
                                                    if (createUserTask.isSuccessful) {
                                                        val userId = createUserTask.result?.user?.uid
                                                        if (userId != null) {
                                                            val user = User(completeName, email, senha, sex, userType)
                                                            val userRef = database.child("users").child(userId)
                                                            userRef.setValue(user).addOnCompleteListener {
                                                                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                                                                if (userType == "Paciente") {
                                                                    val problemsRef = userRef.child("user_problems")
                                                                    val problems = mapOf(
                                                                        "problem1" to "",
                                                                        "problem2" to "",
                                                                        "problem3" to "",
                                                                        "problem4" to ""
                                                                    )
                                                                    problemsRef.setValue(problems)
                                                                }

                                                                val intent = when (userType) {
                                                                    "Paciente" -> Intent(this, Quest_main::class.java)
                                                                    else -> Intent(this, Tela_Inicial::class.java)
                                                                }
                                                                intent.putExtra("USERNAME", completeName)
                                                                startActivity(intent)
                                                                finish()
                                                            }.addOnFailureListener {
                                                                Toast.makeText(this, "Erro ao cadastrar no banco de dados.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    } else {
                                                        Toast.makeText(this, "Erro ao cadastrar usuário: ${createUserTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        }
                                    } else {
                                        Toast.makeText(this, "Erro ao verificar email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
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
