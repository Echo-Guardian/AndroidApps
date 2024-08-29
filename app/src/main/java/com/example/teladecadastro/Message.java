package com.example.teladecadastro;

public class Message {

    private String text;
    private String sender;
    private long timestamp;
    private String audioFilePath;
    private int duration;

    public Message(String text, String sender, long timestamp) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
        this.audioFilePath = null;
        this.duration = 0;
    }

    public Message(String audioFilePath, int duration, String sender, long timestamp) {
        this.audioFilePath = audioFilePath;
        this.duration = duration;
        this.sender = sender;
        this.timestamp = timestamp;
        this.text = null;
    }

    public Message(String text, String sender, long timestamp, boolean isAi) {
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
        this.audioFilePath = null;
        this.duration = 0;
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
        return audioFilePath;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isAudioMessage() {
        return audioFilePath != null;
    }

    public boolean isAiMessage() {
        return text != null && sender.equals("IA"); // Ajuste conforme a lógica do seu aplicativo
    }
}