package com.example.teladecadastro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CameraActivity : AppCompatActivity() {
    private val apiUrl = "http://10.0.2.2:5000"
    private lateinit var streamView: ImageView
    private var isStreaming = false
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val notificationChannelId = "pose_detection_channel"
    private var isStatusCheckEnabled = false
    private var statusJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        streamView = findViewById(R.id.stream_view)

        findViewById<Button>(R.id.start_button).setOnClickListener { startCapture() }
        findViewById<Button>(R.id.stop_button).setOnClickListener { stopCapture() }
        findViewById<Button>(R.id.toggle_status_button).setOnClickListener { toggleStatusCheck() }

        createNotificationChannel()
    }

    private fun startCapture() {
        scope.launch(Dispatchers.IO) {
            try {
                val response = postRequest("$apiUrl/start", """{"room_name":"a"}""")
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        isStreaming = true
                        Toast.makeText(this@CameraActivity, "Captura Iniciada", Toast.LENGTH_SHORT).show()
                        streamFrames()
                    } else {
                        Toast.makeText(this@CameraActivity, "Erro ao Iniciar Captura", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CameraActivity, "Erro ao Iniciar Captura: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun stopCapture() {
        scope.launch(Dispatchers.IO) {
            try {
                val response = postRequest("$apiUrl/stop", """{"room_name":"a"}""")
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        isStreaming = false
                        Toast.makeText(this@CameraActivity, "Captura Parada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CameraActivity, "Erro ao Parar Captura", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CameraActivity, "Erro ao Parar Captura: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun streamFrames() {
        scope.launch(Dispatchers.IO) {
            while (isStreaming) {
                try {
                    val url = URL("$apiUrl/stream")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()

                    if (connection.responseCode == 200) {
                        val inputStream = connection.inputStream
                        val bitmap = BitmapFactory.decodeStream(inputStream)

                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                streamView.setImageBitmap(bitmap)
                            }
                        } else {
                            Log.e("STREAM", "Erro: Bitmap nulo")
                        }
                    } else {
                        val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("STREAM", "Erro HTTP ${connection.responseCode}: $errorStream")
                    }
                } catch (e: Exception) {
                    Log.e("STREAM", "Erro ao buscar stream", e)
                    withContext(Dispatchers.Main) {
                        isStreaming = false
                        Toast.makeText(this@CameraActivity, "Erro ao Exibir Stream", Toast.LENGTH_SHORT).show()
                    }
                }
                delay(500)
            }
        }
    }

    private fun toggleStatusCheck() {
        if (isStatusCheckEnabled) {
            statusJob?.cancel()
            isStatusCheckEnabled = false
            Toast.makeText(this, "Verificação de status desligada", Toast.LENGTH_SHORT).show()
        } else {
            isStatusCheckEnabled = true
            statusJob = lifecycleScope.launch {
                while (isStatusCheckEnabled) {
                    checkStatus()
                    delay(1000)
                }
            }
            Toast.makeText(this, "Verificação de status ligada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStatus() {
        scope.launch(Dispatchers.IO) {
            try {
                val url = URL("$apiUrl/status")
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == 200) {
                    val status = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("STATUS", "Status Atual: $status")
                    checkForPoseDetection(status)
                } else {
                    Log.e("STATUS", "Erro ao obter status: HTTP ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e("STATUS", "Erro ao obter status", e)
            }
        }
    }

    private fun checkForPoseDetection(statusJson: String) {
        try {
            val status = JSONObject(statusJson)
            for (key in status.keys()) {
                if (status.getBoolean(key)) {
                    sendNotification("Câmera $key detectou alguém deitado")
                }
            }
        } catch (e: Exception) {
            Log.e("NOTIFICATION", "Erro ao verificar status", e)
        }
    }

    private fun displayNotification(message: String) {
        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Detecção de Posição")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            }
        }
    }

    private fun sendNotification(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                displayNotification(message)
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        } else {
            displayNotification(message)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Detecção de Posição"
            val descriptionText = "Notificações para detecção de pessoas deitadas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSION", "Permissão de notificações concedida")
            } else {
                Log.e("PERMISSION", "Permissão de notificações negada")
                Toast.makeText(this, "Permissão para enviar notificações foi negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun postRequest(url: String, jsonBody: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.write(jsonBody.toByteArray())
                connection.connect()
                if (connection.responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
