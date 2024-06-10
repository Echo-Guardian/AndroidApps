package com.example.teladecadastro.com.example.teladecadastro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teladecadastro.R
import com.example.teladecadastro.SplashFragment

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SplashFragment())
                .commit()
        }
    }
}
