package com.example.teladecadastro

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
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
import java.io.IOException

class ChatCuidadorActivity : AppCompatActivity() {

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

        databaseHelper = DatabaseCuidador(this)
        messageList = databaseHelper.getAllMessages().toMutableList()

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

        databaseHelper.deleteOldMessages()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
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
            val duration = mediaPlayer!!.duration // em milissegundos

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

        if (messageText.isNotEmpty()) {
            val timestamp = System.currentTimeMillis()
            val sender = "Cuidador"

            databaseHelper.addMessage(messageText, sender, timestamp, 0)

            val message = Message(messageText, sender, timestamp)
            messageList.add(message)
            adapter.notifyItemInserted(messageList.size - 1)

            messageEditText.text.clear()
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        } else {
            Toast.makeText(this, "Digite uma mensagem antes de enviar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearMessages() {
        databaseHelper.deleteAllMessages()
        messageList.clear()
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Mensagens limpas", Toast.LENGTH_SHORT).show()
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