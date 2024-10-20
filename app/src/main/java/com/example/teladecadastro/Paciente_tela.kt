package com.example.teladecadastro

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class Paciente_tela : AppCompatActivity() {

    private var username: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.paciente_tela)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val imageOpenDrawer = findViewById<ImageView>(R.id.image_open_drawer)
        imageOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        username = intent.getStringExtra("USERNAME")
        setGreeting(username)

        updateDrawerHeader()
        displayPatientProblems()

        findViewById<CardView>(R.id.card_emergency).setOnClickListener {
            startActivity(Intent(this, Alerta_main::class.java))
        }

        findViewById<CardView>(R.id.card_avisos).setOnClickListener {
            startActivity(Intent(this, Avisos_main::class.java))
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

    private fun updateDrawerHeader() {
        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val emailTextView = headerView.findViewById<TextView>(R.id.nav_header_email)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.child("name").value as? String ?: "Usuário"
                val email = auth.currentUser?.email ?: "Email não disponível"

                usernameTextView.text = name
                emailTextView.text = email

                val problemsRef = database.child("users").child(userId).child("user_problems")
                problemsRef.get().addOnSuccessListener { problemsSnapshot ->
                    val problems = problemsSnapshot.value as? Map<String, String>
                    problems?.forEach { (key, value) ->
                        Log.d("Problemas", "$key: $value")
                    }
                }
            }.addOnFailureListener {
                Log.e("Paciente_tela", "Erro ao recuperar dados do usuário.")
            }
        } else {
            usernameTextView.text = "Usuário não autenticado"
            emailTextView.text = "Email não disponível"
        }
    }

    private fun displayPatientProblems() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val problemsRef = database.child("users").child(userId).child("user_problems")
            problemsRef.get().addOnSuccessListener { problemsSnapshot ->
                val problems = problemsSnapshot.value as? Map<String, String>
                val problemsList = problems?.values?.joinToString("\n\n") { "• $it" } ?: "Nenhum problema encontrado."

                val idTextView = findViewById<TextView>(R.id.nav_header_id_value)
                idTextView.text = userId

                val problemsTextView = findViewById<TextView>(R.id.nav_header_problemas)
                problemsTextView.text = problemsList
            }.addOnFailureListener {
                Log.e("Paciente_tela", "Erro ao recuperar problemas do usuário.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setGreeting(username)
    }

    private fun setGreeting(username: String?) {
        val greetingTextView = findViewById<TextView>(R.id.text_categorias)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            hour in 6..12 -> "Bom dia"
            hour in 13..18 -> "Boa tarde"
            else -> "Boa noite"
        }

        val userDisplayName = username ?: "usuário"
        greetingTextView.text = "$greeting $userDisplayName"
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar saída")
        builder.setMessage("Você realmente deseja sair da sua conta?")
        builder.setPositiveButton("Sim") { dialog: DialogInterface, _: Int ->
            performLogout()
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
