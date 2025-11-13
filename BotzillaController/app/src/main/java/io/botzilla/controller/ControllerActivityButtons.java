package io.botzilla.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yoimerdr.android.virtualjoystick.geometry.position.ImmutablePosition;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.botzilla.controller.data.ControlPacket;

public class ControllerActivityButtons extends AppCompatActivity {
    private static final String TAG = "ControllerActivityButtons";

    private DatagramSocket udpSocket;
    private InetAddress botzillaIP;
    private volatile boolean running = true;
    private volatile boolean isActive = false;

    private Thread controlThread;
    private ControlPacket dataPacket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controller_b);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

         Intent intent = getIntent();
         String ip = intent.getStringExtra("IP");
         ((TextView) findViewById(R.id.ipTextView)).setText(ip);

        try {
            udpSocket = new DatagramSocket();
            botzillaIP = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        dataPacket = new ControlPacket((byte) 0, (byte) 0, (byte) 0xFF, (byte) 0);

        Log.d(TAG, "onTouch: Released F");

        Button forward = findViewById(R.id.button_forward);
        forward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dataPacket.forward = 127;
                    isActive = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.forward = 0;
                    isActive = false;
                    return true;
            }
            return false;
        });

        Button back = findViewById(R.id.button_back);
        back.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dataPacket.forward = -127;
                    isActive = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.forward = 0;
                    isActive = false;
                    return true;
            }
            return false;
        });

        Button right = findViewById(R.id.button_right);
        right.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dataPacket.turn = 127;
                    isActive = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.turn = 0;
                    isActive = false;
                    return true;
            }
            return false;
        });

        Button left = findViewById(R.id.button_left);
        left.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dataPacket.turn = -127;
                    isActive = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.turn = 0;
                    isActive = false;
                    return true;
            }
            return false;
        });

        SeekBar speed = findViewById(R.id.speed_seek);
        speed.setMax(255);
        speed.setProgress(255);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.speed = (byte) (progress & 0xFF);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startControlLoop();
    }

    private void startControlLoop() {
        controlThread = new Thread(() -> {
            try {
                while (running) {
                    if (isActive) sendControlPacket();

                    Thread.sleep(1000/120);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Control thread interrupted");
            }
        });
        controlThread.start();
    }

    private void sendControlPacket() {
        byte[] buffer = dataPacket.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, botzillaIP, 4210);
        try {
            udpSocket.send(packet);
        } catch (IOException e) {
            Log.e(TAG, "UDP send failed", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        if (controlThread != null) {
            controlThread.interrupt();
        }
        if (udpSocket != null) {
            udpSocket.close();
        }
    }
}