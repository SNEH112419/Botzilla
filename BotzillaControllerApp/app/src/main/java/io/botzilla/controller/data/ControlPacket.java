package io.botzilla.controller.data;

public class ControlPacket {
    public static final int BASE_ROTATION_SERVO = 0;
    public static final int ARM_BJ_SERVO = 1;
    public static final int ARM_ELB_SERVO = 2;
    public static final int ARM_LJ_SERVO = 3;
    public static final int GRIP_ROT_SERVO = 4;
    public static final int GRIP_SERVO_INDEX = 5;


    public byte speed; // has to be from 0 to 255
    public byte flags = 0x0; // unsigned
    public byte[] servos = {90, 0, 10, 0, 90, 0}; // control of servo in the front

    public ControlPacket(int speed, byte flags) {

        this.speed = (byte) (speed & 0xFF);
        this.flags = flags;
    }

    public byte[] getBytes() {
        // If you want a checksum
        byte checksum = (byte) ((speed + flags +
                servos[0] + servos[1] + servos[2] + servos[3] + servos[4] + servos[5]) & 0xFF);

        // Return a byte array: forward, turn, speed, flags, checksum
        return new byte[]{speed, flags,
                servos[0], servos[1], servos[2], servos[3], servos[4], servos[5], checksum};
    }
}
