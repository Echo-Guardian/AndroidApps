package com.example.teladecadastro;

public class Message {

    private String text;
    private String sender;
    private long timestamp;
    private String audioFilePath; // Caminho do arquivo de áudio
    private int duration; // Duração do áudio em milissegundos

    public Message(String text, String sender, long timestamp) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
        this.audioFilePath = null; // Inicializa como null se não for áudio
        this.duration = 0; // Inicializa a duração como 0
    }

    public Message(String audioFilePath, int duration, String sender, long timestamp) {
        this.audioFilePath = audioFilePath;
        this.duration = duration;
        this.sender = sender;
        this.timestamp = timestamp;
        this.text = null; // Pode ser o caminho do áudio
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAudioFilePath() {
        return audioFilePath; // Retorna o caminho do arquivo de áudio
    }

    public int getDuration() {
        return duration; // Retorna a duração do áudio
    }

    // Método para verificar se a mensagem é um áudio
    public boolean isAudioMessage() {
        return audioFilePath != null && audioFilePath.endsWith(".3gp");
    }
}