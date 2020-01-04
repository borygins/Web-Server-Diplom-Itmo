package ru.lb.design.server.ssl;

import ru.lb.impl.server.ssl.SSL;

import java.nio.ByteBuffer;

public interface ISSL {
    void setSSL(ByteBuffer buffer);
    byte[] toByte();
}
