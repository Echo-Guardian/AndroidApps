package com.example.teladecadastro

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CopyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_header)

        val copyButton: ImageButton = findViewById(R.id.btn_copy_id)
        val patientIdTextView: TextView = findViewById(R.id.nav_header_id_value)


        val patientId = patientIdTextView.text.toString()

        copyButton.setOnClickListener {
            if (patientId.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("Patient ID", patientId)
                clipboard.setPrimaryClip(clip)


                Toast.makeText(this, "ID copiada para a área de transferência", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "ID não encontrada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
