package com.example.teladecadastro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatCuidadorActivity extends AppCompatActivity {

    private List<Message> messageList;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageView sendButton;
    private Button btnBack; // Adicionando o botão de voltar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_cuidador);

        // Inicializar a lista de mensagens
        messageList = new ArrayList<>();

        // Inicializar a RecyclerView
        recyclerView = findViewById(R.id.messageRecyclerView);
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar os elementos de entrada de mensagem
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        btnBack = findViewById(R.id.btnBack); // Referenciando o botão de voltar

        // Configurar o clique do botão de envio
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Configurar o clique do botão de voltar
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finaliza a atividade e volta para a atividade anterior
            }
        });
    }

    private void sendMessage() {
        // Obter o texto da mensagem do campo de entrada
        String messageText = messageEditText.getText().toString().trim();

        // Verificar se a mensagem não está vazia
        if (!messageText.isEmpty()) {
            // Criar uma nova mensagem
            long timestamp = System.currentTimeMillis(); // Timestamp da mensagem
            Message message = new Message(messageText, "Cuidador", timestamp);

            // Adicionar a mensagem à lista e notificar o adaptador
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);

            // Limpar o campo de entrada após o envio da mensagem
            messageEditText.setText("");

            // Role automaticamente para a última mensagem
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }
}
