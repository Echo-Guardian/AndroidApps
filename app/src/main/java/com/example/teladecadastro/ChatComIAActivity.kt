
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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ResponseStoppedException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.ArrayList

class ChatComIAActivity : AppCompatActivity() {

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: MutableList<Message>
    private lateinit var generativeModel: GenerativeModel
    private lateinit var databaseHelperIA: DatabaseIA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_com_ia)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#6495ED")

        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        databaseHelperIA = DatabaseIA(this)

        messageList = ArrayList(databaseHelperIA.getAllMessages())
        messageAdapter = MessageAdapter(messageList)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = messageAdapter

        generativeModel = GenerativeModel("gemini-pro", BuildConfig.apiKey)

        sendButton.setOnClickListener {
            sendMessage()
        }

        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener { showPopupMenu(it) }

        databaseHelperIA.deleteOldMessages()
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

    private fun clearMessages() {
        messageList.clear()
        messageAdapter.notifyDataSetChanged()

        databaseHelperIA.clearAllMessages()

        Toast.makeText(this, "Mensagens apagadas", Toast.LENGTH_SHORT).show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val timestamp = System.currentTimeMillis()
            val userMessage = Message(messageText, "Usuário", timestamp)
            messageList.add(userMessage)
            messageAdapter.notifyItemInserted(messageList.size - 1)
            databaseHelperIA.addMessage(messageText, "Usuário", timestamp)

            messageEditText.setText("")

            GlobalScope.launch {
                try {
                    val response = generativeModel.generateContent(messageText)
                    displayResponse(response.text ?: "Resposta nula")
                } catch (e: ResponseStoppedException) {
                    displayResponse("Desculpe, não posso gerar uma resposta para isso.")
                } catch (e: Exception) {
                    displayResponse("Ocorreu um erro ao gerar a resposta.")
                }
            }
        } else {
            Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayResponse(responseText: String) {
        runOnUiThread {
            val aiMessage = Message(responseText, "IA", System.currentTimeMillis())
            messageList.add(aiMessage)
            messageAdapter.notifyItemInserted(messageList.size - 1)
            databaseHelperIA.addMessage(responseText, "IA", System.currentTimeMillis()) // Adicionar ao novo banco de dados
            messageRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }
}