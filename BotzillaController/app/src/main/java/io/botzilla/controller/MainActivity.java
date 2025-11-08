package io.botzilla.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    EditText ipTextEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.startButton = findViewById(R.id.startButton);
        this.ipTextEdit = findViewById(R.id.ipTextEdit);

        this.startButton.setOnClickListener(v -> {
            String ip = ipTextEdit.getText().toString().trim();

            if (!isValidIP(ip)) {
                Toast.makeText(this, "Invalid IP address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check reachability in background
            Toast.makeText(this, "Checking " + ip, Toast.LENGTH_SHORT);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    InetAddress inet = InetAddress.getByName(ip);
                    boolean reachable = inet.isReachable(2000); // timeout 2 sec

                    runOnUiThread(() -> {
                        if (reachable) {
                            Log.d("MAIN_ACTIVITY", "IP reachable: " + ip);
                            Intent controlActivityIntent = new Intent(this, ControllerActivity.class);
                            controlActivityIntent.putExtra("IP", ip);

                            startActivity(controlActivityIntent);
                        } else {
                            Toast.makeText(this, "IP not reachable", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private boolean isValidIP(String ip) {
        // Regex to match IPv4 addresses
        String ipRegex =
                "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}" +
                        "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";
        return ip.matches(ipRegex);
    }
}