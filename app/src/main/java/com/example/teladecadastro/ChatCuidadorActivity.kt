package com.example.teladecadastro

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException

class ChatCuidadorActivity : AppCompatActivity() {

    private lateinit var connectionId: String
    private lateinit var messageList: MutableList<Message>
    private lateinit var adapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var btnBack: Button
    private lateinit var menuIcon: ImageView
    private lateinit var recordButton: ImageView
    private lateinit var databaseHelper: DatabaseCuidador

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFileName: String = ""
    private var isRecording = false

    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_cuidador)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        // Recebe o connectionId automaticamente da Intent
        connectionId = intent.getStringExtra("CONNECTION_ID") ?: run {
            Toast.makeText(this, "ID da conexão não encontrado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseHelper = DatabaseCuidador(this)
        messageList = mutableListOf()

        recyclerView = findViewById(R.id.messageRecyclerView)
        adapter = MessageAdapter(messageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        btnBack = findViewById(R.id.btnBack)
        menuIcon = findViewById(R.id.menuIcon)
        recordButton = findViewById(R.id.mic)

        sendButton.setOnClickListener { sendMessage() }
        btnBack.setOnClickListener { finish() }
        menuIcon.setOnClickListener { showPopupMenu(it) }
        recordButton.setOnClickListener { toggleRecording() }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Configurar listener de mensagens do Firebase
        setupFirebaseListener(connectionId)
    }

    private fun setupFirebaseListener(connectionId: String) {
        val messageRef = FirebaseDatabase.getInstance().reference
            .child("conexoes")
            .child(connectionId)
            .child("messages")

        messageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageText = snapshot.child("text").getValue(String::class.java)
                val sender = snapshot.child("sender").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                if (messageText != null && sender != null) {
                    val message = Message(messageText, sender, timestamp)
                    messageList.add(message)
                    adapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatCuidadorActivity", "Erro ao carregar mensagens: ${error.message}")
                Toast.makeText(this@ChatCuidadorActivity, "Erro ao carregar mensagens: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        audioFileName = "${externalCacheDir?.absolutePath}/audiorecord.3gp"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFileName)

            try {
                prepare()
                start()
                isRecording = true
                recordButton.setImageResource(R.drawable.ic_recording)
                Toast.makeText(this@ChatCuidadorActivity, "Gravação iniciada", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this@ChatCuidadorActivity, "Erro ao iniciar gravação: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                releaseMediaRecorder()
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            isRecording = false
            mediaRecorder = null
            recordButton.setImageResource(R.drawable.ic_audio)
            Toast.makeText(this, "Gravação finalizada", Toast.LENGTH_SHORT).show()

            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(audioFileName)
            mediaPlayer!!.prepare()
            val duration = mediaPlayer!!.duration

            val timestamp = System.currentTimeMillis()
            val sender = "Cuidador"
            databaseHelper.addMessage(audioFileName, sender, timestamp, duration)

            val message = Message(audioFileName, duration, sender, timestamp)
            messageList.add(message)
            adapter.notifyItemInserted(messageList.size - 1)

            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Erro ao finalizar gravação: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            releaseMediaRecorder()
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.apply {
            release()
            mediaRecorder = null
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            release()
            mediaPlayer = null
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_chat, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.clear_conversation -> {
                    showClearConfirmationDialog()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showClearConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar")
            .setMessage("Você realmente deseja limpar a conversa?")
            .setPositiveButton("Sim") { _, _ -> clearMessages() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        Log.d("ChatCuidadorActivity", "Texto da mensagem: $messageText")

        if (messageText.isNotEmpty()) {
            val timestamp = System.currentTimeMillis()
            val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Log.e("ChatCuidadorActivity", "ID do usuário é nulo")
                return
            }

            val messageRef = FirebaseDatabase.getInstance().reference
                .child("conexoes")
                .child(connectionId)
                .child("messages")

            val messageId = messageRef.push().key ?: run {
                Log.e("ChatCuidadorActivity", "Falha ao gerar messageId")
                return
            }

            val messageData = mapOf(
                "text" to messageText,
                "sender" to senderId,
                "timestamp" to timestamp
            )

            messageRef.child(messageId).setValue(messageData)
                .addOnSuccessListener {
                    Log.d("ChatCuidadorActivity", "Mensagem enviada com sucesso: $messageData")
                    Toast.makeText(this, "Mensagem enviada!", Toast.LENGTH_SHORT).show()
                    messageEditText.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.e("ChatCuidadorActivity", "Erro ao enviar mensagem: ${e.message}")
                    Toast.makeText(this, "Erro ao enviar mensagem: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("ChatCuidadorActivity", "Campo de mensagem está vazio")
            Toast.makeText(this, "Digite uma mensagem antes de enviar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearMessages() {
        val messageRef = FirebaseDatabase.getInstance().reference
            .child("conexoes")
            .child(connectionId)
            .child("messages")

        messageRef.removeValue()
            .addOnSuccessListener {
                messageList.clear()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Mensagens apagadas com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao limpar mensagens: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaRecorder()
        releaseMediaPlayer()
    }
}
