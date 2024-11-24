package com.example.teladecadastro

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.text.InputType
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
import com.google.firebase.database.ValueEventListener
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

        // Obtém o ID do cuidador logado no Firebase Authentication
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configuração da interface
        recyclerView = findViewById(R.id.messageRecyclerView)
        messageList = mutableListOf()
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

        // Verifica permissões necessárias
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Inicializa o SharedPreferences para salvar e recuperar o connectionId
        val sharedPreferences = getSharedPreferences("chat_preferences", Context.MODE_PRIVATE)
        val savedConnectionId = sharedPreferences.getString("CONNECTION_ID", null)

        if (savedConnectionId != null) {
            // Reutiliza o connectionId salvo
            connectionId = savedConnectionId
            setupFirebaseListener(connectionId)
        } else {
            // Solicita o ID do paciente e cria ou reutiliza a conexão
            askForPatientIdAndSetupConnection(currentUserId, sharedPreferences)
        }
    }


    private fun createOrGetConnection(cuidadorId: String, patientId: String, callback: (String?) -> Unit) {
        // Gera um identificador único para a conexão
        val connectionKey = "${cuidadorId}-${patientId}"
        val connectionRef = FirebaseDatabase.getInstance().reference.child("conexoes").child(connectionKey)

        connectionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Cria a conexão com os IDs do cuidador e do paciente
                    val connectionData = mapOf(
                        "cuidadorID" to cuidadorId,
                        "pacienteID" to patientId
                    )

                    connectionRef.setValue(connectionData)
                        .addOnSuccessListener {
                            connectionId = connectionKey // Salva o connectionId globalmente
                            callback(connectionKey) // Retorna o connectionId via callback
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                this@ChatCuidadorActivity,
                                "Erro ao criar conexão: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            callback(null)
                        }
                } else {
                    // Se a conexão já existir, apenas reutiliza o connectionId
                    connectionId = connectionKey
                    callback(connectionKey)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ChatCuidadorActivity,
                    "Erro ao acessar conexão: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(null)
            }
        })
    }




    private fun setupFirebaseListener(connectionId: String) {
        val messageRef = FirebaseDatabase.getInstance().reference
            .child("conexoes")
            .child(connectionId)
            .child("messages")

        messageList.clear()
        adapter.notifyDataSetChanged()

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
                Toast.makeText(this@ChatCuidadorActivity, "Erro ao carregar mensagens: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun askForPatientIdAndSetupConnection(cuidadorId: String, sharedPreferences: SharedPreferences) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Abrir Chat")
        dialogBuilder.setMessage("Digite o ID do paciente:")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton("Confirmar") { dialog, _ ->
            val patientId = input.text.toString().trim()

            if (patientId.isEmpty()) {
                Toast.makeText(this, "ID do paciente não pode estar vazio!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setPositiveButton
            }

            // Cria ou obtém a conexão no Firebase
            createOrGetConnection(cuidadorId, patientId) { connectionId ->
                if (connectionId != null) {
                    // Salva o connectionId em SharedPreferences
                    sharedPreferences.edit().putString("CONNECTION_ID", connectionId).apply()

                    // Configura o listener de mensagens
                    setupFirebaseListener(connectionId)
                } else {
                    Toast.makeText(this, "Erro ao configurar a conexão.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
            finish()
        }

        dialogBuilder.show()
    }

    private fun createConnection(patientId: String): String {
        val connectionRef = FirebaseDatabase.getInstance().reference.child("conexoes")
        val newConnectionId = connectionRef.push().key ?: throw IllegalStateException("Erro ao criar conexão")

        val connectionData = mapOf(
            "cuidadorId" to FirebaseAuth.getInstance().currentUser?.uid,
            "patientId" to patientId
        )

        connectionRef.child(newConnectionId).setValue(connectionData)
        return newConnectionId
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
        if (!::connectionId.isInitialized || connectionId.isEmpty()) {
            Toast.makeText(this, "Conexão não inicializada. Por favor, reinicie o chat.", Toast.LENGTH_SHORT).show()
            return
        }

        val messageText = messageEditText.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Mensagem vazia. Por favor, insira texto.", Toast.LENGTH_SHORT).show()
            return
        }

        // Referência ao nó de mensagens dentro da conexão específica
        val messageRef = FirebaseDatabase.getInstance().reference
            .child("conexoes")
            .child(connectionId)
            .child("messages")

        val message = mapOf(
            "text" to messageText,
            "sender" to FirebaseAuth.getInstance().currentUser?.uid,
            "timestamp" to System.currentTimeMillis()
        )

        messageRef.push().setValue(message)
            .addOnSuccessListener {
                messageEditText.text.clear()
                recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar mensagem: ${e.message}", Toast.LENGTH_SHORT).show()
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
