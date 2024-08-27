
package com.example.teladecadastro;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Alerta_main extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alerta_emergencia);

        mediaPlayer = MediaPlayer.create(this, R.raw.som_emergencia);

        Button btnAlert = findViewById(R.id.btnAlerta);
        btnCancel = findViewById(R.id.btnCancelar);
        Button btnVoltar = findViewById(R.id.btnBack);

        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEmergency();
            }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() { // Adicionando o OnClickListener ao botão de voltar
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Tem certeza que deseja ativar o alarme de emergência?")
                .setCancelable(false)
                .setPositiveButton(
                        "Sim",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                activateEmergency();
                            }
                        })
                .setNegativeButton(
                        "Não",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void activateEmergency() {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        if (btnCancel != null) {
            btnCancel.setVisibility(View.VISIBLE);
        }

        Button btnAlert = findViewById(R.id.btnAlerta);
        if (btnAlert != null) {
            btnAlert.setVisibility(View.INVISIBLE);
        }
    }

    private void cancelEmergency() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();
        }

        if (btnCancel != null) {
            btnCancel.setVisibility(View.INVISIBLE);
        }

        Button btnAlert = findViewById(R.id.btnAlerta);
        if (btnAlert != null) {
            btnAlert.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
