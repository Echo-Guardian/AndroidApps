package com.example.teladecadastro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatComIAActivity extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private EditText messageEditText;
    private ImageView sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_com_ia);

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Botão de voltar
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finaliza a atividade e volta para a atividade anterior
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Adicione a mensagem do usuário à lista
            messageList.add(new Message(messageText, true));

            // Atualize o RecyclerView
            messageAdapter.notifyDataSetChanged();

            // Limpe o campo de entrada de mensagem
            messageEditText.setText("");

            // Simule a resposta do IA (apenas para demonstração)
            simulateAIResponse();
        } else {
            // Exibir mensagem de aviso
            Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
        }
    }

    // Método de simulação de resposta do IA (apenas para demonstração)
    private void simulateAIResponse() {
        // Adicione uma mensagem de resposta do IA à lista
        messageList.add(new Message("Olá! Sou o assistente virtual. Como posso ajudar?", false));

        // Atualize o RecyclerView
        messageAdapter.notifyDataSetChanged();
    }
}
