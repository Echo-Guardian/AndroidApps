    package com.example.teladecadastro;

    public class Message {

        private String text;

        public Message(String text, boolean b) {
            this.text = text;
        }

        public Message(String messageText, String cuidador, long timestamp) {
        }

        public String getText() {
            return text;
        }
    }
