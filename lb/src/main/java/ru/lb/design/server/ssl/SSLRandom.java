package ru.lb.design.server.ssl;

import java.nio.ByteBuffer;

public class SSLRandom {
    private long unixTime;
    private byte[] randomBytes = new byte[28];

    public SSLRandom(ByteBuffer byteBuffer) {
        unixTime = byteBuffer.getLong();
        byteBuffer.get(randomBytes);
    }

    public long getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }

    public byte[] getRandomBytes() {
        return randomBytes;
    }

    public void setRandomBytes(byte[] randomBytes) {
        this.randomBytes = randomBytes;
    }
}
