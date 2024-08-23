package com.example.teladecadastro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Avisos_main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.avisos_main)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        val buttonBack = findViewById<Button>(R.id.buttonBack)

        buttonBack.setOnClickListener {
            val intent = Intent()
            // Obtendo o nome do usuário da SharedPreferences da Tela_Inicial
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val username = sharedPreferences.getString("username", "usuário")
            intent.putExtra("USERNAME", username)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
