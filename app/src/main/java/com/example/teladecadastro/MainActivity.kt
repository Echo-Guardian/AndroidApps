package com.example.teladecadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txtCad = findViewById<Button>(R.id.txtCad)

        txtCad.setOnClickListener {
            val intent = Intent(this, Cadastro_main::class.java)
            startActivity(intent)
        }
    }
}
