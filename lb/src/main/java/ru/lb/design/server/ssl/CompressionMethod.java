package ru.lb.design.server.ssl;

public enum CompressionMethod {
    Null(0);

    byte type;

    CompressionMethod(int type) {
        this.type = (byte) type;
    }
    CompressionMethod(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
