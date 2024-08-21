package com.example.teladecadastro;

public class Message {

    private String text;
    private String sender;
    private long timestamp;

    // Construtor que aceita apenas o texto e um booleano (não parece ser usado, mas pode ser mantido se necessário)
    public Message(String text, boolean b) {
        this.text = text;
    }

    // Construtor que aceita texto, remetente e timestamp
    public Message(String text, String sender, long timestamp) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    // Getter para o texto da mensagem
    public String getText() {
        return text;
    }

    // Getter para o remetente da mensagem
    public String getSender() {
        return sender;
    }

    // Getter para o timestamp da mensagem
    public long getTimestamp() {
        return timestamp;
    }
}