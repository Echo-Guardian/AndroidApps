package com.example.teladecadastro


import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
    private lateinit var generativeModel: GenerativeModel  // Declare GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_com_ia)

        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        messageList = ArrayList()
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
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isNotEmpty()) {
            // Add user message to the list
            messageList.add(Message(messageText, true))

            // Update RecyclerView
            messageAdapter.notifyDataSetChanged()

            // Clear message input field
            messageEditText.setText("")

            // Respond using Gemini
            GlobalScope.launch {
                try {
                    val response = generativeModel.generateContent(messageText)
                    displayResponse(response.text ?: "Resposta nula")
                } catch (e: ResponseStoppedException) {
                    Log.e("GenerativeModel", "Content generation stopped: ${e.message}")
                    displayResponse("Desculpe, não posso gerar uma resposta para isso.")
                } catch (e: Exception) {
                    Log.e("GenerativeModel", "Error generating content: ${e.message}")
                    displayResponse("Ocorreu um erro ao gerar a resposta.")
                }
            }
        } else {
            // Display warning message
            Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show()
        }
    }

    // Display response on UI thread
    private fun displayResponse(responseText: String) {
        runOnUiThread {
            messageList.add(Message(responseText, false))
            messageAdapter.notifyDataSetChanged()
            messageRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }
}