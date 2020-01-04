package ru.lb.design.server.ssl;

import java.nio.ByteBuffer;

public interface IHandshake {
    void setSSL(ByteBuffer buffer, short len, HandshakeType typeProtokol);
    byte[] toByte();
}
