package ru.lb.design.server.ssl;

import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

public class SSLRandom implements ISSL {
    private int unixTime;
    private byte[] randomBytes = new byte[32];

    public SSLRandom() {
    }

    public int getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(int unixTime) {
        this.unixTime = unixTime;
    }

    public byte[] getRandomBytes() {
        return randomBytes;
    }

    public void setRandomBytes(byte[] randomBytes) {
        this.randomBytes = randomBytes;
    }

    @Override
    public void setSSL(ByteBuffer buffer) {
        buffer.get(randomBytes);
        unixTime = SSLUtils.convertByteToInt(randomBytes, 4);
    }

    @Override
    public byte[] toByte() {
        return randomBytes;
    }
}
