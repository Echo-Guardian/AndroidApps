package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.Calendar

class Tela_Inicial : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telainicial)

        val username = intent.getStringExtra("USERNAME")
        setGreeting(username)

        val card_emergency = findViewById<CardView>(R.id.card_emergency)
        card_emergency.setOnClickListener {
            val intent = Intent(this, Alerta_main::class.java)
            startActivity(intent)
        }
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
}
