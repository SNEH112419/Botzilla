package io.botzilla.controller.data;

public class ControlPacket {
    public byte forward;
    public byte turn;
    public byte speed; // has to be from 0 to 255
    public byte flags = 0x0; // unsigned
    // byte checksum; // and all and
    public byte elevation = 0; // control of servo in the front
    public byte right = 0; // control right finger
    public byte left = 0; // control left finger

    public ControlPacket(byte forward, byte turn, int speed, byte flags) {
        this.forward = forward;
        this.turn = turn;

        this.speed = (byte) (speed & 0xFF);
        this.flags = flags;
        // this.checksum = (byte) ((this.forward + this.turn + this.speed + this.flags) & 0xFF);
    }

    public byte[] getBytes() {
        // If you want a checksum:
        byte checksum = (byte) ((forward + turn + speed + flags + elevation + left + right) & 0xFF);

        // Return a byte array: forward, turn, speed, flags, checksum
        return new byte[]{forward, turn, speed, flags, elevation, left, right, checksum};
    }
}
