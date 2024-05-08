package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Avisos_main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.avisos_main)

        val buttonBack = findViewById<Button>(R.id.buttonBack)

        buttonBack.setOnClickListener {
            val intent = Intent(this, Tela_Inicial::class.java)
            val username = intent.getStringExtra("USERNAME")
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
    }
}
