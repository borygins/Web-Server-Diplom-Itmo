package ru.lb.design.server.ssl;

public enum ECPointFormatType {
    Uncompressed(0);

    byte type;

    ECPointFormatType(int type) {
        this.type = (byte) type;
    }
    ECPointFormatType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
