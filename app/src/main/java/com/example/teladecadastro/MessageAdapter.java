package com.example.teladecadastro;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_AUDIO = 1;

    private List<Message> messages;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).isAudioMessage()) {
            return TYPE_AUDIO;
        } else {
            return TYPE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_AUDIO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_messege, parent, false);
            return new AudioMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new TextMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AudioMessageViewHolder) {
            ((AudioMessageViewHolder) holder).bind(messages.get(position));
        } else if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(messages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            handler.removeCallbacks(updateSeekBar);
        }
    }

    class AudioMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView playButton;
        private ImageView pauseButton;
        private SeekBar audioSeekBar;
        private TextView audioDurationTextView;

        public AudioMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.playButton);
            pauseButton = itemView.findViewById(R.id.pauseButton);
            audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
            audioDurationTextView = itemView.findViewById(R.id.audioDurationTextView);
        }

        public void bind(Message message) {
            audioDurationTextView.setText(formatDuration(message.getDuration()));

            playButton.setOnClickListener(v -> {
                playAudio(message.getAudioFilePath());
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            });

            pauseButton.setOnClickListener(v -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                }
            });
        }

        private String formatDuration(int duration) {
            int seconds = (duration / 1000) % 60;
            int minutes = (duration / (1000 * 60)) % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }

        private void playAudio(String filePath) {
            releaseMediaPlayer();

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();

                // Configurar o SeekBar e atualizar conforme o áudio é reproduzido
                audioSeekBar.setMax(mediaPlayer.getDuration());
                updateSeekBar = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(updateSeekBar);

                // Quando o áudio terminar
                mediaPlayer.setOnCompletionListener(mp -> {
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                    audioSeekBar.setProgress(0);
                    releaseMediaPlayer();
                });

                audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && mediaPlayer != null) {
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class TextMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(Message message) {
            messageTextView.setText(message.getText());
        }
    }
}