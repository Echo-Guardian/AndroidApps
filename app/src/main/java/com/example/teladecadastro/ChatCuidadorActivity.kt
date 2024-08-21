package com.example.teladecadastro

import android.app.AlertDialog
import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatCuidadorActivity : AppCompatActivity() {

    private lateinit var messageList: MutableList<Message>
    private lateinit var adapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var btnBack: Button
    private lateinit var menuIcon: ImageView // Ícone de três pontos
    private lateinit var databaseHelper: DatabaseCuidador

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
        menuIcon = findViewById(R.id.menuIcon) // Inicializando o ícone de menu

        sendButton.setOnClickListener { sendMessage() }
        btnBack.setOnClickListener { finish() }

        // Ícone de três pontos
        menuIcon.setOnClickListener { showPopupMenu(it) }

        // Remove mensagens antigas
        databaseHelper.deleteOldMessages()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_chat, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
            R.id.clear_conversation -> {
                showClearConfirmationDialog() // Chama o método de confirmação
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
            databaseHelper.addMessage(messageText, sender, timestamp)

            val message = Message(messageText, sender, timestamp)
            messageList.add(message)
            adapter.notifyItemInserted(messageList.size - 1)

            messageEditText.text.clear() // Limpa o campo de texto
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        } else {
            Toast.makeText(this, "Digite uma mensagem antes de enviar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearMessages() {
        databaseHelper.deleteAllMessages() // Chama o método para deletar todas as mensagens
        messageList.clear() // Limpa a lista de mensagens na UI
        adapter.notifyDataSetChanged() // Notifica o adaptador sobre a mudança
        Toast.makeText(this, "Mensagens limpas", Toast.LENGTH_SHORT).show() // Exibe mensagem de confirmação
    }
}