package ru.lb.design.server.ssl;

public enum ContentType {
    Handshake(22),ApplicationData(23),ChangeCipherSpec(20);

    byte type;

    ContentType(int type) {
        this.type = (byte) type;
    }
    ContentType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
