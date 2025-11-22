package io.botzilla.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yoimerdr.android.virtualjoystick.geometry.position.ImmutablePosition;
import com.yoimerdr.android.virtualjoystick.views.JoystickView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.botzilla.controller.data.ControlPacket;

public class ControllerActivityJoystick extends AppCompatActivity {

    private static final String TAG = "ControllerActivity";
    private JoystickView joystick;
    private DatagramSocket udpSocket;
    private InetAddress botzillaIP;
    private volatile boolean running = true;
    private Thread controlThread;

    private ControlPacket dataPacket;

    SeekBar elevation_seek;
    SeekBar grip_seek;

    int grip_val;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controller);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String ip = intent.getStringExtra("IP");
        ((TextView) findViewById(R.id.ipTextViewB)).setText(ip);

        try {
            udpSocket = new DatagramSocket();
            botzillaIP = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        dataPacket = new ControlPacket((byte) 0, (byte) 0, (byte) 0xFF, (byte) 0);

        joystick = findViewById(R.id.vJoystick);
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

        elevation_seek = findViewById(R.id.elevation_seek);
        grip_seek = findViewById(R.id.grip_seek);

        elevation_seek.setMax(120);
        elevation_seek.setProgress(0);
        elevation_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.elevation = (byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        grip_seek.setMax(180);
        grip_seek.setProgress(0);
        grip_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                grip_val = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        startControlLoop();
    }

    private void startControlLoop() {
        controlThread = new Thread(() -> {
            try {
                while (running) {
                    sendControlPacket();
                    Thread.sleep(0, 100); // 50 Hz update rate (20 ms per frame)
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Control thread interrupted");
            }
        });
        controlThread.start();
    }

    private void sendControlPacket() {
        ImmutablePosition pos = joystick.getNdcPosition();
        byte forward = (byte) (-127 * pos.getY());
        byte turn = (byte) (127 * pos.getX());
        dataPacket.forward = forward;
        dataPacket.turn = turn;
        dataPacket.grip = (byte) (grip_val & 0xFF);
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
