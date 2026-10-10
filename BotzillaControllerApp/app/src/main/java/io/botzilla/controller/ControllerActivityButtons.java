package io.botzilla.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private volatile int vibration_request_tracker = 0;
    // 1st for forward button
    // 2nd for back button
    // 3rd for left button
    // 4th for right button
    // opt. 5th for elevation button

    private Thread controlThread;
    private ControlPacket dataPacket;
    private Vibrator vibrator;

    // UI elements
    SeekBar base_rot_seek;
    SeekBar grip_rot_seek;
    SeekBar grip_seek, lj_seek, elb_seek, bj_seek;

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
         ((TextView) findViewById(R.id.ipTextViewB)).setText(ip);

        try {
            udpSocket = new DatagramSocket();
            botzillaIP = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        dataPacket = new ControlPacket(0xFF, (byte) 0);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Button forward = findViewById(R.id.button_forward);
        forward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // set fprward and reset back
                    dataPacket.flags &= ~(1 << 1);
                    dataPacket.flags |= 1;
                    vibration_request_tracker |= 1;
                    return true;
                case MotionEvent.ACTION_UP:
                    // reset both forward and backworkd
                    dataPacket.flags &= ~(1 & (1 << 1));
                    vibration_request_tracker &= ~1;
                    return true;
            }
            return false;
        });

        Button back = findViewById(R.id.button_back);
        back.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:dataPacket.flags |= 1;
                    dataPacket.flags &= ~(1);
                    dataPacket.flags |= (1 << 1);
                    vibration_request_tracker |= (1<<1);
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.flags &= ~(1 & (1 << 1));
                    vibration_request_tracker &= ~(1<<1);
                    return true;
            }
            return false;
        });

        Button right = findViewById(R.id.button_right);
        right.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dataPacket.flags &= ~(1 << 2);
                    dataPacket.flags |= (1 << 3);
                    vibration_request_tracker |= (1<<2);
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.flags &= ~((1 << 2) & (1 << 3));
                    vibration_request_tracker &= ~(1<<2);
                    return true;
            }
            return false;
        });

        Button left = findViewById(R.id.button_left);
        left.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dataPacket.flags &= ~(1 << 3);
                    dataPacket.flags |= (1 << 2);
                    vibration_request_tracker |= (1<<2);
                    return true;
                case MotionEvent.ACTION_UP:
                    dataPacket.flags &= ~((1 << 2) & (1 << 3));
                    vibration_request_tracker &= ~(1<<2);
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
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        base_rot_seek = findViewById(R.id.base_rot_seek);
        base_rot_seek.setMax(180);
        base_rot_seek.setProgress(90);
        base_rot_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.servos[ControlPacket.BASE_ROTATION_SERVO] = (byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        grip_seek = findViewById(R.id.grip_seek);
        grip_seek.setMax(45);
        grip_seek.setProgress(0);
        grip_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.servos[ControlPacket.GRIP_SERVO_INDEX] =(byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        grip_rot_seek = findViewById(R.id.grip_rot_seek);
        grip_rot_seek.setMax(180);
        grip_rot_seek.setProgress(90);
        grip_rot_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.servos[ControlPacket.GRIP_ROT_SERVO] = (byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        bj_seek = findViewById(R.id.arm_bj_seek);
        bj_seek.setMax(180);
        bj_seek.setProgress(0);
        bj_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.servos[ControlPacket.ARM_BJ_SERVO] = (byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        elb_seek = findViewById(R.id.arm_elb_seek);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            elb_seek.setMin(10);
        }
        elb_seek.setMax(180);
        elb_seek.setProgress(10);
        elb_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.servos[ControlPacket.ARM_ELB_SERVO] = (byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        lj_seek = findViewById(R.id.arm_lj_seek);
        lj_seek.setMax(90);
        lj_seek.setProgress(0);
        lj_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dataPacket.servos[ControlPacket.ARM_LJ_SERVO] = (byte) (progress & 0xFF);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        startControlLoop();
    }

    private void startControlLoop() {
        controlThread = new Thread(() -> {
            try {
                while (running) {
                    sendControlPacket();
                    control_vibration();
                    Thread.sleep(1000/50);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Control thread interrupted", e);
            }
        });
        controlThread.start();
    }

    private void sendControlPacket() {
        if (vibration_request_tracker == 0) {
            dataPacket.speed = (byte) 0;
        }

        byte[] buffer = dataPacket.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, botzillaIP, 4210);
        try {
            udpSocket.send(packet);
        } catch (IOException e) {
            Log.e(TAG, "UDP send failed", e);
        }
    }

    private int prev_vibration_tracker = 0;
    private boolean isVibrating = false;
    private void control_vibration() {
        if (prev_vibration_tracker == 0 && vibration_request_tracker != 0) {
            Log.d(TAG, "control_vibration: Trying Vibration");
            startVibration();
        }
        if (prev_vibration_tracker != 0 && vibration_request_tracker == 0) {
            Log.d(TAG, "control_vibration: Stopping Vibration");
            stopVibration();
        }
        prev_vibration_tracker = vibration_request_tracker;
    }

    private void startVibration() {
        Log.d(TAG, "startVibration: Trying to vibrate");
        if (vibrator == null || isVibrating) return;

        long[] pattern = {0, 20, 15}; // engine idle feel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
            );
        } else {
            vibrator.vibrate(pattern, 0);
        }

        isVibrating = true;
        Log.d(TAG, "startVibration: At the end of function");
    }

    private void stopVibration() {
        if (vibrator == null || !isVibrating) return;
        vibrator.cancel();
        isVibrating = false;
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