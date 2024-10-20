package com.example.teladecadastro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class Cuidador_tela : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cuidador_tela)

        auth = FirebaseAuth.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        updateDrawerHeader()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_link_patient -> {
                    showLinkPatientDialog()
                    true
                }
                R.id.item1 -> {
                    true
                }
                R.id.item2 -> {
                    true
                }
                else -> false
            }
        }

        val imageOpenDrawer = findViewById<ImageView>(R.id.image_open_drawer)
        imageOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        setGreeting()
        configureCardViews()
    }

    override fun onResume() {
        super.onResume()
        setGreeting()
    }

    private fun updateDrawerHeader() {
        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val emailTextView = headerView.findViewById<TextView>(R.id.nav_header_email)

        val user = auth.currentUser
        if (user != null) {
            Log.d("Cuidador_tela", "Usuário autenticado: ${user.uid}")
            emailTextView.text = user.email ?: "Email não disponível"

            if (user.displayName != null && user.displayName!!.isNotEmpty()) {
                usernameTextView.text = user.displayName
            } else {
                fetchUserNameFromFirestore(user.uid, usernameTextView)
            }
        } else {
            Log.d("Cuidador_tela", "Usuário não autenticado")
            usernameTextView.text = "Nome não disponível"
            emailTextView.text = "Email não disponível"
        }
    }

    private fun fetchUserNameFromFirestore(userId: String, usernameTextView: TextView) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    usernameTextView.text = name ?: "Nome não disponível"
                    Log.d("Cuidador_tela", "Nome do usuário obtido do Firestore: $name")
                } else {
                    usernameTextView.text = "Documento não encontrado"
                    Log.d("Cuidador_tela", "Documento não encontrado no Firestore")
                }
            }
            .addOnFailureListener { e ->
                usernameTextView.text = "Erro ao obter nome"
                Log.e("Cuidador_tela", "Erro ao obter o nome do Firestore: ${e.message}")
            }
    }

    private fun setGreeting() {
        val greetingTextView = findViewById<TextView>(R.id.text_categorias)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            hour in 6..12 -> "Bom dia"
            hour in 13..18 -> "Boa tarde"
            else -> "Boa noite"
        }

        val userDisplayName = auth.currentUser?.displayName ?: "usuário"
        greetingTextView.text = "$greeting $userDisplayName"
    }

    private fun showLinkPatientDialog() {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Vincular paciente")
        dialog.setMessage("Digite a ID do paciente:")
        dialog.setView(input)

        dialog.setPositiveButton("Vincular") { _, _ ->
            val patientId = input.text.toString()
            if (patientId.isNotEmpty()) {
                linkPatient(patientId)
            } else {
                Toast.makeText(this, "ID do paciente não pode estar vazia.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setNegativeButton("Cancelar", null)
        dialog.show()
    }

    private fun linkPatient(patientId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val linkRef = db.collection("cuidador_pacientes").document(userId).collection("pacientes").document(patientId)
            linkRef.set(mapOf("linked" to true)).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Paciente vinculado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao vincular paciente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun configureCardViews() {
        findViewById<CardView>(R.id.card_avisos).setOnClickListener {
            startActivity(Intent(this, Avisos_main::class.java))
        }

        findViewById<CardView>(R.id.camera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        findViewById<CardView>(R.id.card_chat_cuidador).setOnClickListener {
            startActivity(Intent(this, ChatCuidadorActivity::class.java))
        }

        findViewById<CardView>(R.id.card_chat_ia).setOnClickListener {
            startActivity(Intent(this, ChatComIAActivity::class.java))
        }

        findViewById<CardView>(R.id.card_sair).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar saída")
        builder.setMessage("Você realmente deseja sair da sua conta?")
        builder.setPositiveButton("Sim") { _, _ -> performLogout() }
        builder.setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun performLogout() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
