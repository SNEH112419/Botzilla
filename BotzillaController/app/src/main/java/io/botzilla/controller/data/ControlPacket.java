package io.botzilla.controller.data;

public class ControlPacket {
    byte forward;
    byte turn;
    byte speed; // has to be from 0 to 255
    byte flags = 0x0; // unsigned
    // byte checksum; // and all and

    public ControlPacket(byte forward, byte turn, int speed, byte flags) {
        this.forward = forward;
        this.turn = turn;

        this.speed = (byte) (speed & 0xFF);
        this.flags = flags;
        // this.checksum = (byte) ((this.forward + this.turn + this.speed + this.flags) & 0xFF);
    }

    public byte[] getBytes() {
        // If you want a checksum:
        byte checksum = (byte) ((forward + turn + speed + flags) & 0xFF);

        // Return a byte array: forward, turn, speed, flags, checksum
        return new byte[]{forward, turn, speed, flags, checksum};
    }
}
