package ru.lb.design.server.ssl;

public enum HandshakeType {
    ClientHello(1), ServerHello(2),
    ClientKeyExchange(16),CertificateStatus(22),
    Certificate(11),
    ServerKeyExchage(12),ServerHelloDone(14);

    byte type;

    HandshakeType(int type) {
        this.type = (byte) type;
    }
    HandshakeType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
